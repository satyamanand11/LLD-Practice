package com.lld.hotel.management.pattern.composite;

import com.lld.hotel.management.entities.Room;
import com.lld.hotel.management.entities.RoomType;
import com.lld.hotel.management.entities.RoomStatus;
import com.lld.hotel.management.entities.DateRange;
import com.lld.hotel.management.entities.RoomAvailability;

import java.util.Collections;
import java.util.List;

/**
 * Composite Pattern - Leaf
 * Represents a single room
 */
public class RoomLeaf implements RoomComponent {
    private final Room room;
    private final RoomAvailability availability;

    public RoomLeaf(Room room, RoomAvailability availability) {
        if (room == null) {
            throw new IllegalArgumentException("room cannot be null");
        }
        if (availability == null) {
            throw new IllegalArgumentException("availability cannot be null");
        }
        this.room = room;
        this.availability = availability;
    }

    @Override
    public List<Room> getRooms() {
        return Collections.singletonList(room);
    }

    @Override
    public List<Room> searchByType(RoomType roomType) {
        if (room.getRoomType() == roomType) {
            return Collections.singletonList(room);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Room> searchByStatus(RoomStatus status) {
        if (room.getStatus() == status) {
            return Collections.singletonList(room);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Room> searchAvailable(DateRange dateRange) {
        if (room.getStatus() == RoomStatus.AVAILABLE && 
            availability.isAvailable(dateRange)) {
            return Collections.singletonList(room);
        }
        return Collections.emptyList();
    }

    @Override
    public int getRoomCount() {
        return 1;
    }

    @Override
    public String getName() {
        return room.getRoomNumber();
    }

    public Room getRoom() {
        return room;
    }
}

