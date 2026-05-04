package com.lld.bms.facade;

import com.lld.bms.domain.Booking;
import com.lld.bms.domain.City;
import com.lld.bms.domain.Movie;
import com.lld.bms.domain.Show;
import com.lld.bms.domain.ShowSeat;
import com.lld.bms.domain.User;
import com.lld.bms.domain.Screen;
import com.lld.bms.domain.ScreenType;
import com.lld.bms.domain.Seat;
import com.lld.bms.domain.SeatType;
import com.lld.bms.domain.Venue;
import com.lld.bms.repo.BookingRepository;
import com.lld.bms.repo.CityRepository;
import com.lld.bms.repo.InMemoryBookingRepository;
import com.lld.bms.repo.InMemoryCityRepository;
import com.lld.bms.repo.InMemoryMovieRepository;
import com.lld.bms.repo.InMemoryScreenRepository;
import com.lld.bms.repo.InMemoryShowRepository;
import com.lld.bms.repo.InMemoryShowSeatRepository;
import com.lld.bms.repo.InMemoryUserRepository;
import com.lld.bms.repo.InMemoryVenueRepository;
import com.lld.bms.repo.MovieRepository;
import com.lld.bms.repo.ScreenRepository;
import com.lld.bms.repo.ShowRepository;
import com.lld.bms.repo.ShowSeatRepository;
import com.lld.bms.repo.UserRepository;
import com.lld.bms.repo.VenueRepository;
import com.lld.bms.service.BookingService;
import com.lld.bms.service.CityService;
import com.lld.bms.service.MovieService;
import com.lld.bms.service.ShowService;
import com.lld.bms.service.UserService;
import com.lld.bms.service.VenueService;
import com.lld.bms.service.locking.SeatLockManager;
import com.lld.bms.service.pricing.PriceModifier;
import com.lld.bms.service.pricing.PricingService;
import com.lld.bms.service.pricing.SurgeModifier;
import com.lld.bms.service.pricing.WeekendModifier;
import com.lld.bms.service.selection.SeatSelection;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Singleton facade. Wires the in-memory implementation:
 * - ConcurrentHashMap-backed repos
 * - SeatLockManager for per-seat synchronized monitors
 * - Tier prices stored on ShowSeat at show creation
 * - Demand modifiers (weekend, surge) applied at booking by PricingService
 */
public class BookMyShowSystemImpl implements BookMyShowSystem {

    private static volatile BookMyShowSystemImpl instance;

    private final UserService userService;
    private final CityService cityService;
    private final VenueService venueService;
    private final MovieService movieService;
    private final ShowService showService;
    private final BookingService bookingService;

    public BookMyShowSystemImpl(UserService userService,
                                CityService cityService,
                                VenueService venueService,
                                MovieService movieService,
                                ShowService showService,
                                BookingService bookingService) {
        this.userService = Objects.requireNonNull(userService);
        this.cityService = Objects.requireNonNull(cityService);
        this.venueService = Objects.requireNonNull(venueService);
        this.movieService = Objects.requireNonNull(movieService);
        this.showService = Objects.requireNonNull(showService);
        this.bookingService = Objects.requireNonNull(bookingService);
    }

    public static BookMyShowSystemImpl getInstance() {
        if (instance == null) {
            synchronized (BookMyShowSystemImpl.class) {
                if (instance == null) {
                    instance = buildDefault();
                }
            }
        }
        return instance;
    }

    private static BookMyShowSystemImpl buildDefault() {
        UserRepository userRepo = new InMemoryUserRepository();
        CityRepository cityRepo = new InMemoryCityRepository();
        VenueRepository venueRepo = new InMemoryVenueRepository();
        ScreenRepository screenRepo = new InMemoryScreenRepository();
        MovieRepository movieRepo = new InMemoryMovieRepository();
        ShowRepository showRepo = new InMemoryShowRepository();
        ShowSeatRepository showSeatRepo = new InMemoryShowSeatRepository();
        BookingRepository bookingRepo = new InMemoryBookingRepository();

        Clock clock = Clock.systemDefaultZone();
        SeatLockManager seatLockManager = new SeatLockManager();

        UserService users = new UserService(userRepo);
        CityService cities = new CityService(cityRepo);
        VenueService venues = new VenueService(venueRepo, screenRepo, cityRepo);
        MovieService movies = new MovieService(movieRepo);
        ShowService shows = new ShowService(showRepo, showSeatRepo, screenRepo, movieRepo,
                seatLockManager, defaultTierPrices(), clock);
        PricingService pricing = new PricingService(defaultModifiers(shows));
        BookingService bookings = new BookingService(bookingRepo, shows, users, pricing, clock);

        return new BookMyShowSystemImpl(users, cities, venues, movies, shows, bookings);
    }

    public static Map<SeatType, Integer> defaultTierPrices() {
        Map<SeatType, Integer> prices = new EnumMap<>(SeatType.class);
        prices.put(SeatType.SILVER, 150);
        prices.put(SeatType.GOLD, 250);
        prices.put(SeatType.PLATINUM, 400);
        return prices;
    }

    private static List<PriceModifier> defaultModifiers(ShowService shows) {
        return List.of(
                new WeekendModifier(120),
                new SurgeModifier(shows::occupancyPercent, 70, 125)
        );
    }

    @Override
    public User registerUser(String name, String email) { return userService.registerUser(name, email); }

    @Override
    public User getUser(String userId) { return userService.getUser(userId); }

    @Override
    public City addCity(String name, String state) { return cityService.addCity(name, state); }

    @Override
    public List<City> listCities() { return cityService.listCities(); }

    @Override
    public Venue addVenue(String cityId, String name, String address) {
        return venueService.addVenue(cityId, name, address);
    }

    @Override
    public List<Venue> listVenuesInCity(String cityId) { return venueService.listVenuesInCity(cityId); }

    @Override
    public Screen addScreen(String venueId, String name, ScreenType type, List<Seat> seats) {
        return venueService.addScreen(venueId, name, type, seats);
    }

    @Override
    public List<Screen> listScreens(String venueId) { return venueService.listScreens(venueId); }

    @Override
    public Movie addMovie(String title, int durationMinutes, String genre, String language) {
        return movieService.addMovie(title, durationMinutes, genre, language);
    }

    @Override
    public List<Movie> searchMovies(String titleFragment) { return movieService.searchByTitle(titleFragment); }

    @Override
    public List<Movie> listMovies() { return movieService.listAll(); }

    @Override
    public Show createShow(String movieId, String screenId, LocalDateTime startTime, LocalDateTime endTime) {
        return showService.createShow(movieId, screenId, startTime, endTime);
    }

    @Override
    public Show getShow(String showId) { return showService.getShow(showId); }

    @Override
    public List<Show> listShowsForMovie(String movieId) { return showService.listShowsForMovie(movieId); }

    @Override
    public List<Show> listShowsAtVenue(String venueId) { return showService.listShowsAtVenue(venueId); }

    @Override
    public List<ShowSeat> listAvailableSeats(String showId) { return showService.listAvailableSeats(showId); }

    @Override
    public Booking bookSeats(String userId, String showId, List<String> showSeatIds) {
        return bookingService.bookSeats(userId, showId, showSeatIds);
    }

    @Override
    public Booking bookSelections(String userId, String showId, List<SeatSelection> selections) {
        return bookingService.bookSelections(userId, showId, selections);
    }

    @Override
    public Booking cancelBooking(String confirmationId) { return bookingService.cancelBooking(confirmationId); }

    @Override
    public Booking getBooking(String confirmationId) { return bookingService.getBooking(confirmationId); }

    @Override
    public List<Booking> listBookingsForUser(String userId) { return bookingService.listBookingsForUser(userId); }
}
