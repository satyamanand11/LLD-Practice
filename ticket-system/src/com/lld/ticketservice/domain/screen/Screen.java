package com.lld.ticketservice.domain.screen;

import com.lld.ticketservice.domain.seat.Seat;

import java.util.List;


public class Screen {
    private final int screenNumber;
    private final int theatreId;
    private final List<Seat> seats;

    public Screen(int screenNumber, int theatreId, List<Seat> seats) {
        this.screenNumber = screenNumber;
        this.theatreId = theatreId;
        this.seats = List.copyOf(seats);
    }

    public int getScreenNumber() {
        return screenNumber;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public List<Seat> getSeats() {
        return seats;
    }
}
