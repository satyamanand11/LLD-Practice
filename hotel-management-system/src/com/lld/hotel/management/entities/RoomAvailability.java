package com.lld.hotel.management.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomAvailability {

    private final int roomId;
    private final List<DateRange> reservedRanges = new ArrayList<>();

    public RoomAvailability(int roomId) {
        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }
        this.roomId = roomId;
    }

    public void reserve(DateRange requestedRange) {

        for (DateRange existing : reservedRanges) {
            if (existing.overlaps(requestedRange)) {
                throw new IllegalStateException(
                        "Room already reserved for overlapping dates"
                );
            }
        }
        reservedRanges.add(requestedRange);
    }

    public void release(DateRange range) {
        if (!reservedRanges.remove(range)) {
            throw new IllegalStateException(
                    "Reservation does not exist for given date range"
            );
        }
    }

    public boolean isAvailable(DateRange requestedRange) {
        for (DateRange existing : reservedRanges) {
            if (existing.overlaps(requestedRange)) {
                return false;
            }
        }
        return true;
    }

    public List<DateRange> getReservedRanges() {
        return Collections.unmodifiableList(reservedRanges);
    }

    public int getRoomId() {
        return roomId;
    }
}
