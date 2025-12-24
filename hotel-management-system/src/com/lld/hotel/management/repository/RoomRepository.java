package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    Optional<Room> findById(int roomId);
    List<Room> findAll();
    void save(Room room);
}
