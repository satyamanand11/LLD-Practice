package com.lld.amazon.locker.repository;

import com.lld.amazon.locker.model.Locker;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLockerRepository implements LockerRepository {

    private final ConcurrentHashMap<String, Locker> lockers = new ConcurrentHashMap<>();

    @Override
    public Optional<Locker> findById(String lockerId) {
        return Optional.ofNullable(lockers.get(lockerId));
    }

    @Override
    public void save(Locker locker) {
        lockers.put(locker.getLockerId(), locker);
    }
}
