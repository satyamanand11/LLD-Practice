package com.lld.ticketservice.managers.booking;

import com.lld.ticketservice.domain.booking.Booking;

public interface BookingManager {
    void save(Booking b);

    Booking get(int bookingId);

    void updateStatus(int bookingId, String status);
}
