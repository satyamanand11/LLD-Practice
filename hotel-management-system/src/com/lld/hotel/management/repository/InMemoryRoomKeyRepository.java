package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.RoomKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoomKeyRepository implements RoomKeyRepository {

    private final Map<Integer, RoomKey> store = new ConcurrentHashMap<>();

    @Override
    public Optional<RoomKey> findById(int keyId) {
        return Optional.ofNullable(store.get(keyId));
    }

    @Override
    public void save(RoomKey key) {
        store.put(key.getKeyId(), key);
    }
}
