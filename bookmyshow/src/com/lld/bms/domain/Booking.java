package com.lld.bms.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Booking {
    private final String id;
    private final String userId;
    private final String showId;
    private final List<String> showSeatIds;
    private final String confirmationId;
    private final int totalAmount;
    private final LocalDateTime createdAt;
    private BookingStatus status;
    private LocalDateTime cancelledAt;

    public Booking(String id, String userId, String showId,
                   List<String> showSeatIds, String confirmationId,
                   int totalAmount, LocalDateTime createdAt) {
        if (totalAmount < 0) {
            throw new IllegalArgumentException("totalAmount must be >= 0");
        }
        this.id = id;
        this.userId = userId;
        this.showId = showId;
        this.showSeatIds = List.copyOf(showSeatIds);
        this.confirmationId = confirmationId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getShowId() { return showId; }
    public List<String> getShowSeatIds() { return Collections.unmodifiableList(showSeatIds); }
    public String getConfirmationId() { return confirmationId; }
    public int getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }

    public void cancel(LocalDateTime now) {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }
        if (now == null) throw new IllegalArgumentException("now cannot be null");
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = now;
    }
}
