package com.lld.ticketservice.decorator;

public class ReclinerDecorator extends SeatDecorator {
    public ReclinerDecorator(SeatComponent inner) {
        super(inner);
    }

    public int getPrice() {
        return inner.getPrice() + 200;
    }

    public String getDescription() {
        return inner.getDescription() + " + Recliner";
    }
}
