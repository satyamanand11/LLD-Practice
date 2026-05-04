package com.lld.bms.repo;

import com.lld.bms.domain.Booking;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryBookingRepository implements BookingRepository {
    private final ConcurrentHashMap<String, Booking> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> confirmationIndex = new ConcurrentHashMap<>();

    @Override
    public void save(Booking booking) {
        store.put(booking.getId(), booking);
        confirmationIndex.put(booking.getConfirmationId(), booking.getId());
    }

    @Override
    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Booking> findByConfirmationId(String confirmationId) {
        String id = confirmationIndex.get(confirmationId);
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        return store.values().stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
