package com.lld.ticketservice.managers.seat;

import com.lld.ticketservice.domain.seat.Seat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SeatManagerImpl implements SeatManager {
    private final Map<String, List<Seat>> byScreen = new ConcurrentHashMap<>();

    private String key(int v, int s) {
        return v + ":" + s;
    }

    public void registerSeats(int venueId, int screenNumber, List<Seat> seats) {
        byScreen.put(key(venueId, screenNumber), List.copyOf(seats));
    }

    public List<Seat> getSeatsForScreen(int venueId, int screenNumber) {
        return byScreen.getOrDefault(key(venueId, screenNumber), List.of());
    }

    public int getSeatBasePrice(int venueId, int screenNumber, int seatNumber) {
        return getSeatsForScreen(venueId, screenNumber).stream().filter(seat -> seat.getNumber() == seatNumber).findFirst().map(Seat::basePrice).orElseThrow();
    }
}
