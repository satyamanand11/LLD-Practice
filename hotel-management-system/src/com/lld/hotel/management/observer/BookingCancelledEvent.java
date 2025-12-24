package com.lld.hotel.management.observer;

import java.time.LocalDateTime;

public class BookingCancelledEvent implements DomainEvent {

    private final int bookingId;
    private final LocalDateTime occurredAt = LocalDateTime.now();

    public BookingCancelledEvent(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getBookingId() { return bookingId; }

    public LocalDateTime occurredAt() { return occurredAt; }
}
