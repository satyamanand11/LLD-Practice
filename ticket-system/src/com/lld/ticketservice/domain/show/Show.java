package com.lld.ticketservice.domain.show;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Show {
    private final int showId;
    private final int venueId;
    private final int screenNumber;
    private final int eventId;
    private final Map<Integer, ShowSeat> seats = new ConcurrentHashMap<>();

    public Show(int showId, int venueId, int screenNumber, int eventId, int seatCount) {
        this.showId = showId;
        this.venueId = venueId;
        this.screenNumber = screenNumber;
        this.eventId = eventId;
        for (int i = 1; i <= seatCount; i++) seats.put(i, new ShowSeat(i));
    }

    public int getShowId() {
        return showId;
    }

    public int getVenueId() {
        return venueId;
    }

    public int getScreenNumber() {
        return screenNumber;
    }

    public int getEventId() {
        return eventId;
    }

    public Map<Integer, ShowSeat> getSeats() {
        return seats;
    }

    public int occupancyPercent() {
        long booked = seats.values().stream().filter(s -> s.getStatus() == ShowSeatStatus.BOOKED).count();
        return (int) Math.round((booked * 100.0) / seats.size());
    }
}
