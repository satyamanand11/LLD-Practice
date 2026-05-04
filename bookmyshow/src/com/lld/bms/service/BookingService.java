package com.lld.bms.service;

import com.lld.bms.domain.Booking;
import com.lld.bms.domain.BookingStatus;
import com.lld.bms.domain.Show;
import com.lld.bms.domain.ShowSeat;
import com.lld.bms.repo.BookingRepository;
import com.lld.bms.service.pricing.PricingService;
import com.lld.bms.service.selection.SeatSelection;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowService showService;
    private final PricingService pricingService;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          ShowService showService,
                          UserService userService,
                          PricingService pricingService,
                          Clock clock) {
        this.bookingRepository = Objects.requireNonNull(bookingRepository);
        this.showService = Objects.requireNonNull(showService);
        this.pricingService = Objects.requireNonNull(pricingService);
        this.clock = Objects.requireNonNull(clock);
    }

    public Booking bookSeats(String userId, String showId, List<String> showSeatIds) {
        List<SeatSelection> selections = new ArrayList<>(showSeatIds.size());
        for (String id : showSeatIds) {
            selections.add(new SeatSelection(id));
        }
        return bookSelections(userId, showId, selections);
    }

    public Booking bookSelections(String userId, String showId, List<SeatSelection> selections) {
        if (selections == null || selections.isEmpty()) {
            throw new IllegalArgumentException("selections cannot be null or empty");
        }

        Map<String, SeatSelection> bySeatId = new HashMap<>();
        for (SeatSelection sel : selections) {
            if (bySeatId.put(sel.showSeatId(), sel) != null) {
                throw new IllegalArgumentException("Duplicate seat: " + sel.showSeatId());
            }
        }
        List<String> showSeatIds = new ArrayList<>(bySeatId.keySet());

        Show show = showService.getShow(showId);

        // Phase 1: brief lock — AVAILABLE -> LOCKED
        List<ShowSeat> reserved = showService.reserveSeats(showId, showSeatIds, userId);

        // Payment runs HERE (out of scope, assume success). No JVM lock held;
        // seats stay held via status=LOCKED + lockedUntil.

        int total = 0;
        for (ShowSeat seat : reserved) {
            SeatSelection sel = bySeatId.get(seat.getId());
            total += pricingService.price(seat, show, sel.addOns());
        }

        // Phase 2: brief lock — LOCKED -> BOOKED
        showService.confirmReservation(showId, showSeatIds, userId);

        String confirmationId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Booking booking = new Booking(
                UUID.randomUUID().toString(), userId, showId, showSeatIds,
                confirmationId, total, LocalDateTime.now(clock));
        bookingRepository.save(booking);
        return booking;
    }

    public Booking cancelBooking(String confirmationId) {
        Booking booking = bookingRepository.findByConfirmationId(confirmationId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + confirmationId));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled: " + confirmationId);
        }
        showService.releaseSeats(booking.getShowId(), booking.getShowSeatIds());
        booking.cancel(LocalDateTime.now(clock));
        bookingRepository.save(booking);
        return booking;
    }

    public Booking getBooking(String confirmationId) {
        return bookingRepository.findByConfirmationId(confirmationId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + confirmationId));
    }

    public List<Booking> listBookingsForUser(String userId) {
        return bookingRepository.findByUserId(userId);
    }
}
