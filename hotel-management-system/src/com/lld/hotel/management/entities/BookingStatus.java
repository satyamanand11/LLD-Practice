package com.lld.hotel.management.entities;

public enum BookingStatus {
    CREATED,        // Booking created, payment not yet confirmed
    CONFIRMED,      // Payment completed, booking confirmed
    CHECKED_IN,     // Guest checked in
    CHECKED_OUT,    // Guest checked out
    CANCELLED
}
