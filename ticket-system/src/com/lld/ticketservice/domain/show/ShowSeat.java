package com.lld.ticketservice.domain.show;

public class ShowSeat {
    private final int seatNumber;
    private ShowSeatStatus status = ShowSeatStatus.VACANT;
    private String lockedByUser;
    private long lockExpiresAt;

    public ShowSeat(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus s) {
        this.status = s;
    }

    public String getLockedByUser() {
        return lockedByUser;
    }

    public void setLockedByUser(String u) {
        this.lockedByUser = u;
    }

    public long getLockExpiresAt() {
        return lockExpiresAt;
    }

    public void setLockExpiresAt(long t) {
        this.lockExpiresAt = t;
    }
}
