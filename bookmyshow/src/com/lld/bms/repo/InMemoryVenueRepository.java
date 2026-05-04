package com.lld.bms.repo;

import com.lld.bms.domain.Venue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryVenueRepository implements VenueRepository {
    private final ConcurrentHashMap<String, Venue> store = new ConcurrentHashMap<>();

    @Override
    public void save(Venue venue) {
        store.put(venue.getId(), venue);
    }

    @Override
    public Optional<Venue> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Venue> findByCityId(String cityId) {
        return store.values().stream()
                .filter(v -> v.getCityId().equals(cityId))
                .collect(Collectors.toList());
    }
}
