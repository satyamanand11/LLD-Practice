package com.lld.ticketservice.managers.seat;

import com.lld.ticketservice.domain.seat.Seat;

import java.util.List;

public interface SeatManager {
    void registerSeats(int venueId, int screenNumber, List<Seat> seats);

    List<Seat> getSeatsForScreen(int venueId, int screenNumber);

    int getSeatBasePrice(int venueId, int screenNumber, int seatNumber);
}
