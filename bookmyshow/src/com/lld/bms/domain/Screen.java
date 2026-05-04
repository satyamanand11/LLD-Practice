package com.lld.bms.domain;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Screen {
    private final String id;
    private final String venueId;
    private final String name;
    private final ScreenType type;
    private final List<Seat> seats;

    public Screen(String id, String venueId, String name, ScreenType type, List<Seat> seats) {
        this.id = id;
        this.venueId = venueId;
        this.name = name;
        this.type = type;
        this.seats = List.copyOf(seats);
    }

    public String getId() { return id; }
    public String getVenueId() { return venueId; }
    public String getName() { return name; }
    public ScreenType getType() { return type; }
    public List<Seat> getSeats() { return Collections.unmodifiableList(seats); }

    public Optional<Seat> findSeat(String seatId) {
        return seats.stream().filter(s -> s.getId().equals(seatId)).findFirst();
    }
}
