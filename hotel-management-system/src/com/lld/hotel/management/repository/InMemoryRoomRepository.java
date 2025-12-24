package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoomRepository implements RoomRepository {

    private final Map<Integer, Room> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Room> findById(int roomId) {
        return Optional.ofNullable(store.get(roomId));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void save(Room room) {
        store.put(room.getRoomId(), room);
    }
}
