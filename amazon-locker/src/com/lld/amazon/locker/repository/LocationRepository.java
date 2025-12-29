package com.lld.amazon.locker.repository;

import com.lld.amazon.locker.model.LockerLocation;

import java.util.Optional;

public interface LocationRepository {
    Optional<LockerLocation> findById(String locationId);
    void save(LockerLocation location);
}
