package com.lld.hotel.management.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder Pattern for Booking construction
 * Ensures valid object creation with fluent API
 */
public class BookingBuilder {
    private int bookingId;
    private int guestAccountId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Integer> serviceIds = new ArrayList<>();

    public BookingBuilder setBookingId(int bookingId) {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("bookingId must be positive");
        }
        this.bookingId = bookingId;
        return this;
    }

    public BookingBuilder setGuest(int guestAccountId) {
        if (guestAccountId <= 0) {
            throw new IllegalArgumentException("guestAccountId must be positive");
        }
        this.guestAccountId = guestAccountId;
        return this;
    }

    public BookingBuilder setRoom(int roomId) {
        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }
        this.roomId = roomId;
        return this;
    }

    public BookingBuilder setDates(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("check-in and check-out dates are required");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate must be after checkInDate");
        }
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        return this;
    }

    public BookingBuilder addService(int serviceId) {
        if (serviceId <= 0) {
            throw new IllegalArgumentException("serviceId must be positive");
        }
        this.serviceIds.add(serviceId);
        return this;
    }

    public Booking build() {
        validate();
        return new Booking(bookingId, guestAccountId, roomId, checkInDate, checkOutDate);
    }

    private void validate() {
        if (bookingId <= 0) {
            throw new IllegalStateException("bookingId is required");
        }
        if (guestAccountId <= 0) {
            throw new IllegalStateException("guestAccountId is required");
        }
        if (roomId <= 0) {
            throw new IllegalStateException("roomId is required");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalStateException("check-in and check-out dates are required");
        }
    }

    public List<Integer> getServiceIds() {
        return new ArrayList<>(serviceIds);
    }
}

