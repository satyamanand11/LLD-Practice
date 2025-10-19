package com.lld.ticketservice.managers.booking;

import com.lld.ticketservice.domain.booking.Booking;
import com.lld.ticketservice.domain.booking.BookingStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingManagerImpl implements BookingManager {
    private final Map<Integer, Booking> store = new ConcurrentHashMap<>();

    public void save(Booking b) {
        store.put(b.getBookingId(), b);
    }

    public Booking get(int id) {
        return store.get(id);
    }

    public void updateStatus(int id, String status) {
        store.get(id).setStatus(Enum.valueOf(BookingStatus.class, status));
    }
}
