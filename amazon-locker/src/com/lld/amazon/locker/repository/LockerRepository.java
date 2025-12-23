package com.lld.amazon.locker.repository;

import com.lld.amazon.locker.model.Locker;

import java.util.Optional;

public interface LockerRepository {
    Optional<Locker> findById(String lockerId);
    void save(Locker locker);
}
