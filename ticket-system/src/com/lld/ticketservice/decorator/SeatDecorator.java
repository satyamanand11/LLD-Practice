package com.lld.ticketservice.decorator;

public abstract class SeatDecorator implements SeatComponent {
    protected final SeatComponent inner;

    protected SeatDecorator(SeatComponent inner) {
        this.inner = inner;
    }
}
