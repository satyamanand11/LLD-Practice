package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.BookingServiceEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryBookingServiceRepository implements BookingServiceRepository {
    private final Map<Integer, BookingServiceEntity> store = new ConcurrentHashMap<>();

    @Override
    public Optional<BookingServiceEntity> findById(int bookingServiceId) {
        return Optional.ofNullable(store.get(bookingServiceId));
    }

    @Override
    public List<BookingServiceEntity> findByBookingId(int bookingId) {
        return store.values().stream()
                .filter(bs -> bs.getBookingId() == bookingId)
                .collect(Collectors.toList());
    }

    @Override
    public void save(BookingServiceEntity bookingService) {
        store.put(bookingService.getBookingServiceId(), bookingService);
    }

    @Override
    public void delete(int bookingServiceId) {
        store.remove(bookingServiceId);
    }
}

