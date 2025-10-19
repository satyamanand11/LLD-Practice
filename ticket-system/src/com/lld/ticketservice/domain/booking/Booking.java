package com.lld.ticketservice.domain.booking;

import java.util.List;

public class Booking {
    private final int bookingId;
    private final int showId;
    private final String userId;
    private final List<Integer> seatNumbers;
    private BookingStatus status;

    public Booking(int bookingId, int showId, String userId, List<Integer> seatNumbers) {
        this.bookingId = bookingId;
        this.showId = showId;
        this.userId = userId;
        this.seatNumbers = List.copyOf(seatNumbers);
        this.status = BookingStatus.PENDING;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getShowId() {
        return showId;
    }

    public String getUserId() {
        return userId;
    }

    public List<Integer> getSeatNumbers() {
        return seatNumbers;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus s) {
        this.status = s;
    }
}
