package com.lld.ticketservice.decorator;

public class FoodComboDecorator extends SeatDecorator {
    public FoodComboDecorator(SeatComponent inner) {
        super(inner);
    }

    public int getPrice() {
        return inner.getPrice() + 50;
    }

    public String getDescription() {
        return inner.getDescription() + " + Combo";
    }
}
