package com.lld.amazon.locker.repository;

import com.lld.amazon.locker.model.LockerLocation;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLocationRepository implements LocationRepository {

    private final ConcurrentHashMap<String, LockerLocation> locations = new ConcurrentHashMap<>();

    @Override
    public Optional<LockerLocation> findById(String locationId) {
        return Optional.ofNullable(locations.get(locationId));
    }

    @Override
    public void save(LockerLocation location) {
        locations.put(location.getLocationId(), location);
    }
}
