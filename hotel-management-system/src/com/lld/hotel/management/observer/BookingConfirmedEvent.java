package com.lld.hotel.management.observer;

import java.time.LocalDateTime;

public class BookingConfirmedEvent implements DomainEvent {

    private final int bookingId;
    private final int guestAccountId;
    private final LocalDateTime occurredAt = LocalDateTime.now();

    public BookingConfirmedEvent(int bookingId, int guestAccountId) {
        this.bookingId = bookingId;
        this.guestAccountId = guestAccountId;
    }

    public int getBookingId() { return bookingId; }
    public int getGuestAccountId() { return guestAccountId; }

    public LocalDateTime occurredAt() { return occurredAt; }
}
