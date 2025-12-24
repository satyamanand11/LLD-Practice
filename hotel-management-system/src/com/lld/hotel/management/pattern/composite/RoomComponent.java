package com.lld.hotel.management.pattern.composite;

import com.lld.hotel.management.entities.Room;
import com.lld.hotel.management.entities.RoomType;
import com.lld.hotel.management.entities.RoomStatus;
import com.lld.hotel.management.entities.DateRange;

import java.util.List;

/**
 * Composite Pattern - Component Interface
 * Uniform interface for individual rooms and room groups
 */
public interface RoomComponent {
    /**
     * Get all rooms in this component
     */
    List<Room> getRooms();

    /**
     * Search rooms by type
     */
    List<Room> searchByType(RoomType roomType);

    /**
     * Search rooms by status
     */
    List<Room> searchByStatus(RoomStatus status);

    /**
     * Search available rooms for date range
     */
    List<Room> searchAvailable(DateRange dateRange);

    /**
     * Get total count of rooms
     */
    int getRoomCount();

    /**
     * Get component name/identifier
     */
    String getName();
}

