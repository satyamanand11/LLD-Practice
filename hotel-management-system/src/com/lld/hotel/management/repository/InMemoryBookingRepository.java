package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Booking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Consumer;

public class InMemoryBookingRepository
        implements BookingRepository {

    private final Map<Integer, Booking> store =
            new ConcurrentHashMap<>();

    private final Map<Integer, Object> locks =
            new ConcurrentHashMap<>();

    @Override
    public void executeWithLock(
            int bookingId,
            Consumer<Booking> action) {

        Object lock = locks.computeIfAbsent(bookingId, k -> new Object());

        synchronized (lock) {
            Booking booking = store.get(bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking not found");
            }

            action.accept(booking);
            store.put(bookingId, booking);
        }
    }

    @Override
    public void create(Booking booking) {
        store.put(booking.getBookingId(), booking);
    }
}
