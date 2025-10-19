package com.lld.ticketservice.decorator;

public class DefaultSeatComponent implements SeatComponent {
    private final int basePrice;
    private final String description;

    public DefaultSeatComponent(int basePrice, String description) {
        this.basePrice = basePrice;
        this.description = description;
    }

    public int getPrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }
}
