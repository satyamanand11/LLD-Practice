package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.RoomAvailability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Consumer;

public class InMemoryRoomAvailabilityRepository implements RoomAvailabilityRepository {

    private final Map<Integer, RoomAvailability> store = new ConcurrentHashMap<>();

    private final Map<Integer, Object> locks = new ConcurrentHashMap<>();

    @Override
    public void executeWithLock(
            int roomId,
            Consumer<RoomAvailability> action) {

        Object lock = locks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
            RoomAvailability availability = store.getOrDefault(roomId, new RoomAvailability(roomId));

            action.accept(availability);
            store.put(roomId, availability);
        }
    }
}