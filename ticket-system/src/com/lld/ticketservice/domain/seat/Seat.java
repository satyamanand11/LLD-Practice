package com.lld.ticketservice.domain.seat;

import com.lld.ticketservice.decorator.SeatComponent;

public class Seat {
    private final int number;
    private final SeatType type;
    private final SeatComponent component;

    public Seat(int number, SeatType type, SeatComponent component) {
        this.number = number;
        this.type = type;
        this.component = component;
    }

    public int getNumber() {
        return number;
    }

    public SeatType getType() {
        return type;
    }

    public SeatComponent getComponent() {
        return component;
    }

    public int basePrice() {
        return component.getPrice();
    }

    public String label() {
        return component.getDescription();
    }
}