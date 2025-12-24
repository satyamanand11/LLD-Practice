package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.RoomKey;

import java.util.Optional;

public interface RoomKeyRepository {
    Optional<RoomKey> findById(int keyId);
    void save(RoomKey key);
}
