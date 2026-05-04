package com.lld.bms.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class ShowSeat {
    private final String id;
    private final String showId;
    private final String seatId;
    private final int basePrice;
    private SeatStatus status;
    private String lockedBy;
    private LocalDateTime lockedUntil;

    public ShowSeat(String id, String showId, String seatId, int basePrice) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("basePrice must be >= 0");
        }
        this.id = id;
        this.showId = showId;
        this.seatId = seatId;
        this.basePrice = basePrice;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getId() { return id; }
    public String getShowId() { return showId; }
    public String getSeatId() { return seatId; }
    public int getBasePrice() { return basePrice; }
    public SeatStatus getStatus() { return status; }
    public String getLockedBy() { return lockedBy; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }

    public void lock(String userId, LocalDateTime now, LocalDateTime until) {
        if (status == SeatStatus.BOOKED) {
            throw new IllegalStateException("Cannot lock seat in status: " + status);
        }
        if (status == SeatStatus.LOCKED && !isLockExpired(now)) {
            throw new IllegalStateException("Cannot lock seat in status: " + status);
        }
        this.status = SeatStatus.LOCKED;
        this.lockedBy = userId;
        this.lockedUntil = until;
    }

    public void confirmBooking(String userId) {
        if (status != SeatStatus.LOCKED) {
            throw new IllegalStateException("Cannot book seat in status: " + status);
        }
        if (!Objects.equals(lockedBy, userId)) {
            throw new IllegalStateException("Lock is held by a different user");
        }
        this.status = SeatStatus.BOOKED;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
    }

    public boolean isLockExpired(LocalDateTime now) {
        return status == SeatStatus.LOCKED && lockedUntil != null && now.isAfter(lockedUntil);
    }
}
