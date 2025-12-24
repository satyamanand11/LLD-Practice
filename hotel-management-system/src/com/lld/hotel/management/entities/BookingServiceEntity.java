package com.lld.hotel.management.entities;

import java.math.BigDecimal;

/**
 * BookingService Entity (R8)
 * Links bookings with additional services
 */
public class BookingServiceEntity {
    private final int bookingServiceId;
    private final int bookingId;
    private final int serviceId;
    private final int quantity;
    private final BigDecimal price;
    private BookingServiceStatus status;

    public enum BookingServiceStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    public BookingServiceEntity(int bookingServiceId, int bookingId, 
                                int serviceId, int quantity, BigDecimal price) {
        if (bookingServiceId <= 0) {
            throw new IllegalArgumentException("bookingServiceId must be positive");
        }
        if (bookingId <= 0) {
            throw new IllegalArgumentException("bookingId must be positive");
        }
        if (serviceId <= 0) {
            throw new IllegalArgumentException("serviceId must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must be non-negative");
        }

        this.bookingServiceId = bookingServiceId;
        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.price = price;
        this.status = BookingServiceStatus.PENDING;
    }

    public void activate() {
        if (status != BookingServiceStatus.PENDING) {
            throw new IllegalStateException("Can only activate from PENDING status");
        }
        this.status = BookingServiceStatus.ACTIVE;
    }

    public void complete() {
        if (status != BookingServiceStatus.ACTIVE) {
            throw new IllegalStateException("Can only complete from ACTIVE status");
        }
        this.status = BookingServiceStatus.COMPLETED;
    }

    public void cancel() {
        if (status == BookingServiceStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed service");
        }
        this.status = BookingServiceStatus.CANCELLED;
    }

    public int getBookingServiceId() {
        return bookingServiceId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public BookingServiceStatus getStatus() {
        return status;
    }
}

