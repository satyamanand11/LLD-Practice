package com.lld.ticketservice.facade;

import com.lld.ticketservice.domain.booking.Booking;
import com.lld.ticketservice.domain.event.Event;
import com.lld.ticketservice.domain.show.Show;
import com.lld.ticketservice.domain.ticket.Ticket;
import com.lld.ticketservice.managers.event.EventManager;
import com.lld.ticketservice.managers.show.ShowManager;
import com.lld.ticketservice.services.BookingService;
import com.lld.ticketservice.services.PricingService;
import com.lld.ticketservice.services.TicketService;

import java.util.List;
import java.util.Map;

public final class TicketSystem {
    private static volatile TicketSystem INSTANCE;

    // Initialize once at startup
    public static TicketSystem init(EventManager events, ShowManager shows,
                                    PricingService pricing, BookingService booking,
                                    TicketService ticketService) {
        TicketSystem local = INSTANCE;
        if (local == null) {
            synchronized (TicketSystem.class) {
                local = INSTANCE;
                if (local == null) {
                    INSTANCE = local = new TicketSystem(events, shows, pricing, booking, ticketService);
                }
            }
        }
        return local;
    }

    public static TicketSystem getInstance() {
        TicketSystem local = INSTANCE;
        if (local == null) throw new IllegalStateException("TicketSystem not initialized. Call init(...) first.");
        return local;
    }

    private final EventManager events;
    private final ShowManager shows;
    private final PricingService pricing;
    private final BookingService booking;
    private final TicketService ticketService;

    private TicketSystem(EventManager events, ShowManager shows,
                          PricingService pricing, BookingService booking,
                          TicketService ticketService) {
        this.events = events;
        this.shows = shows;
        this.pricing = pricing;
        this.booking = booking;
        this.ticketService = ticketService;
    }

    public List<Event> listEvents() {
        return events.listAll();
    }

    public List<Show> listShows(int eventId) {
        return shows.getShowsByEvent(eventId);
    }

    public Map<Integer, Integer> quoteSeats(int showId, List<Integer> seats) {
        return pricing.quoteSeats(showId, seats);
    }

    public Booking reserve(String userId, int showId, List<Integer> seats, long holdTtlMs) {
        return booking.reserve(userId, showId, seats, holdTtlMs);
    }

    public Ticket confirmAndIssue(Booking b, String userId) {
        booking.confirmSeats(b.getBookingId(), userId);
        int total = pricing.totalForSeats(b.getShowId(), b.getSeatNumbers());
        return ticketService.issue(b.getBookingId(), total, userId);
    }

    public void cancel(int bookingId) {
        booking.cancel(bookingId);
    }

    public void printShowState(int showId) {
        System.out.println("Show " + showId + " -> " + booking.showState(showId));
    }
}
