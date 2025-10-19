package com.lld.ticketservice.decorator;

public class SofaDecorator extends SeatDecorator {
    public SofaDecorator(SeatComponent inner) {
        super(inner);
    }

    public int getPrice() {
        return inner.getPrice() + 100;
    }

    public String getDescription() {
        return inner.getDescription() + " + Sofa";
    }
}
