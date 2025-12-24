package com.lld.hotel.management.pattern.composite;

import com.lld.hotel.management.entities.Room;
import com.lld.hotel.management.entities.RoomType;
import com.lld.hotel.management.entities.RoomStatus;
import com.lld.hotel.management.entities.DateRange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Composite Pattern - Composite
 * Represents a group of rooms (by floor, style, etc.)
 */
public class RoomGroup implements RoomComponent {
    private final String name;
    private final List<RoomComponent> children = new ArrayList<>();

    public RoomGroup(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        this.name = name;
    }

    public void add(RoomComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
        children.add(component);
    }

    public void remove(RoomComponent component) {
        children.remove(component);
    }

    @Override
    public List<Room> getRooms() {
        return children.stream()
                .flatMap(child -> child.getRooms().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> searchByType(RoomType roomType) {
        return children.stream()
                .flatMap(child -> child.searchByType(roomType).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> searchByStatus(RoomStatus status) {
        return children.stream()
                .flatMap(child -> child.searchByStatus(status).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> searchAvailable(DateRange dateRange) {
        return children.stream()
                .flatMap(child -> child.searchAvailable(dateRange).stream())
                .collect(Collectors.toList());
    }

    @Override
    public int getRoomCount() {
        return children.stream()
                .mapToInt(RoomComponent::getRoomCount)
                .sum();
    }

    @Override
    public String getName() {
        return name;
    }

    public List<RoomComponent> getChildren() {
        return new ArrayList<>(children);
    }
}

