package com.lld.bms.service;

import com.lld.bms.domain.Movie;
import com.lld.bms.domain.SeatStatus;
import com.lld.bms.domain.Show;
import com.lld.bms.domain.ShowSeat;
import com.lld.bms.domain.ShowStatus;
import com.lld.bms.domain.Screen;
import com.lld.bms.domain.Seat;
import com.lld.bms.domain.SeatType;
import com.lld.bms.repo.MovieRepository;
import com.lld.bms.repo.ScreenRepository;
import com.lld.bms.repo.ShowRepository;
import com.lld.bms.repo.ShowSeatRepository;
import com.lld.bms.service.locking.SeatLockManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShowService {
    private static final long LOCK_TTL_SECONDS = 60L;

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final SeatLockManager seatLockManager;
    private final Map<SeatType, Integer> tierPrices;
    private final Clock clock;

    public ShowService(ShowRepository showRepository,
                       ShowSeatRepository showSeatRepository,
                       ScreenRepository screenRepository,
                       MovieRepository movieRepository,
                       SeatLockManager seatLockManager,
                       Map<SeatType, Integer> tierPrices,
                       Clock clock) {
        this.showRepository = Objects.requireNonNull(showRepository);
        this.showSeatRepository = Objects.requireNonNull(showSeatRepository);
        this.screenRepository = Objects.requireNonNull(screenRepository);
        this.movieRepository = Objects.requireNonNull(movieRepository);
        this.seatLockManager = Objects.requireNonNull(seatLockManager);
        Objects.requireNonNull(tierPrices, "tierPrices cannot be null");
        if (tierPrices.isEmpty()) {
            throw new IllegalArgumentException("tierPrices cannot be empty");
        }
        this.tierPrices = new EnumMap<>(tierPrices);
        this.clock = Objects.requireNonNull(clock);
    }

    public Show createShow(String movieId, String screenId, LocalDateTime startTime, LocalDateTime endTime) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NoSuchElementException("Movie not found: " + movieId));
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new NoSuchElementException("Screen not found: " + screenId));

        Show show = new Show(
                UUID.randomUUID().toString(),
                movie.getId(), screen.getId(),
                startTime, endTime,
                ShowStatus.SCHEDULED,
                LocalDateTime.now(clock));
        showRepository.save(show);

        for (Seat seat : screen.getSeats()) {
            Integer basePrice = tierPrices.get(seat.getType());
            if (basePrice == null) {
                throw new IllegalStateException("No tier price configured for seat type: " + seat.getType());
            }
            ShowSeat showSeat = new ShowSeat(UUID.randomUUID().toString(), show.getId(), seat.getId(), basePrice);
            showSeatRepository.save(showSeat);
        }
        return show;
    }

    public Show getShow(String showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new NoSuchElementException("Show not found: " + showId));
    }

    public List<Show> listShowsForMovie(String movieId) {
        return showRepository.findByMovieId(movieId);
    }

    public List<Show> listShowsAtVenue(String venueId) {
        return screenRepository.findByVenueId(venueId).stream()
                .flatMap(s -> showRepository.findByScreenId(s.getId()).stream())
                .collect(Collectors.toList());
    }

    public List<ShowSeat> listAvailableSeats(String showId) {
        return showSeatRepository.findByShowId(showId).stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public int occupancyPercent(String showId) {
        List<ShowSeat> all = showSeatRepository.findByShowId(showId);
        if (all.isEmpty()) {
            return 0;
        }
        long booked = all.stream().filter(s -> s.getStatus() != SeatStatus.AVAILABLE).count();
        return (int) (booked * 100 / all.size());
    }

    public List<ShowSeat> reserveSeats(String showId, List<String> showSeatIds, String userId) {
        Show show = getShow(showId);
        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new IllegalStateException("Show is not bookable in status: " + show.getStatus());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime lockUntil = now.plusSeconds(LOCK_TTL_SECONDS);
        return seatLockManager.executeWithLocks(showSeatIds, () -> {
            List<ShowSeat> seats = loadShowSeats(showId, showSeatIds);
            for (ShowSeat seat : seats) {
                seat.lock(userId, now, lockUntil);
                showSeatRepository.save(seat);
            }
            return seats;
        });
    }

    public void confirmReservation(String showId, List<String> showSeatIds, String userId) {
        seatLockManager.executeWithLocks(showSeatIds, () -> {
            List<ShowSeat> seats = loadShowSeats(showId, showSeatIds);
            for (ShowSeat seat : seats) {
                seat.confirmBooking(userId);
                showSeatRepository.save(seat);
            }
            return seats;
        });
    }

    public void releaseSeats(String showId, List<String> showSeatIds) {
        seatLockManager.executeWithLocks(showSeatIds, () -> {
            List<ShowSeat> seats = loadShowSeats(showId, showSeatIds);
            for (ShowSeat seat : seats) {
                seat.release();
                showSeatRepository.save(seat);
            }
            return null;
        });
    }

    private List<ShowSeat> loadShowSeats(String showId, List<String> showSeatIds) {
        List<ShowSeat> seats = showSeatRepository.findByIds(showSeatIds);
        if (seats.size() != showSeatIds.size()) {
            throw new NoSuchElementException("ShowSeat not found among: " + showSeatIds);
        }
        for (ShowSeat s : seats) {
            if (!s.getShowId().equals(showId)) {
                throw new IllegalArgumentException("ShowSeat " + s.getId() + " does not belong to show " + showId);
            }
        }
        return seats;
    }
}
