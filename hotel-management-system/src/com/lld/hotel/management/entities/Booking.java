package com.lld.hotel.management.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Booking {

    private final int bookingId;
    private final int guestAccountId;
    private final int roomId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private BookingStatus status;
    private boolean paymentCompleted;

    public Booking(int bookingId, int guestAccountId, int roomId,
                   LocalDate checkInDate, LocalDate checkOutDate) {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("bookingId must be positive");
        }
        if (guestAccountId <= 0) {
            throw new IllegalArgumentException("guestAccountId must be positive");
        }
        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("check-in and check-out dates are required");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException(
                    "checkOutDate must be after checkInDate"
            );
        }

        this.bookingId = bookingId;
        this.guestAccountId = guestAccountId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;

        this.status = BookingStatus.CREATED;
        this.paymentCompleted = false;
    }

    public void markPaymentCompleted() {
        this.paymentCompleted = true;
    }

    public void confirm() {
        if (!paymentCompleted) {
            throw new IllegalStateException("Cannot confirm booking without completed payment");
        }
        if (status != BookingStatus.CREATED) {
            throw new IllegalStateException(
                    "Booking can only be confirmed from CREATED state"
            );
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void checkIn() {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking must be CONFIRMED before check-in");
        }
        this.status = BookingStatus.CHECKED_IN;
    }

    public void checkOut() {
        if (status != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Booking must be CHECKED_IN before check-out");
        }
        this.status = BookingStatus.CHECKED_OUT;
    }

    public void cancel(LocalDateTime cancellationTime) {
        Objects.requireNonNull(cancellationTime, "cancellationTime is required");

        if (status == BookingStatus.CHECKED_IN || status == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException(
                    "Cannot cancel booking after check-in"
            );
        }
        this.status = BookingStatus.CANCELLED;
    }

    public boolean isEligibleForFullRefund(LocalDateTime cancellationTime) {
        Objects.requireNonNull(cancellationTime, "cancellationTime is required");

        return cancellationTime.isBefore(checkInDate.atStartOfDay().minusHours(24));
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getGuestAccountId() {
        return guestAccountId;
    }

    public int getRoomId() {
        return roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }
}