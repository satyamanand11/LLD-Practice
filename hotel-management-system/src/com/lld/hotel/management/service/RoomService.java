package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.*;
import com.lld.hotel.management.pattern.composite.RoomComponent;
import com.lld.hotel.management.pattern.composite.RoomGroup;
import com.lld.hotel.management.pattern.composite.RoomLeaf;
import com.lld.hotel.management.repository.RoomRepository;
import com.lld.hotel.management.repository.RoomAvailabilityRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomService - Manages room operations and search
 * Uses Composite pattern for room catalog management
 */
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository availabilityRepository;
    private RoomComponent roomCatalog;

    public RoomService(RoomRepository roomRepository, 
                      RoomAvailabilityRepository availabilityRepository) {
        this.roomRepository = roomRepository;
        this.availabilityRepository = availabilityRepository;
        buildRoomCatalog();
    }

    private void buildRoomCatalog() {
        RoomGroup hotel = new RoomGroup("Hotel");
        
        // Group by room type
        RoomGroup standardGroup = new RoomGroup("Standard Rooms");
        RoomGroup deluxeGroup = new RoomGroup("Deluxe Rooms");
        RoomGroup familySuiteGroup = new RoomGroup("Family Suites");
        RoomGroup businessSuiteGroup = new RoomGroup("Business Suites");

        List<Room> allRooms = roomRepository.findAll();
        for (Room room : allRooms) {
            RoomAvailability availability = getOrCreateAvailability(room.getRoomId());
            RoomLeaf roomLeaf = new RoomLeaf(room, availability);

            switch (room.getRoomType()) {
                case STANDARD:
                    standardGroup.add(roomLeaf);
                    break;
                case DELUXE:
                    deluxeGroup.add(roomLeaf);
                    break;
                case FAMILY_SUITE:
                    familySuiteGroup.add(roomLeaf);
                    break;
                case BUSINESS_SUITE:
                    businessSuiteGroup.add(roomLeaf);
                    break;
            }
        }

        hotel.add(standardGroup);
        hotel.add(deluxeGroup);
        hotel.add(familySuiteGroup);
        hotel.add(businessSuiteGroup);

        this.roomCatalog = hotel;
    }

    private RoomAvailability getOrCreateAvailability(int roomId) {
        final RoomAvailability[] availability = new RoomAvailability[1];
        availabilityRepository.executeWithLock(roomId, avail -> {
            availability[0] = avail;
        });
        if (availability[0] == null) {
            availability[0] = new RoomAvailability(roomId);
        }
        return availability[0];
    }

    public List<Room> searchRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("check-in and check-out dates are required");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate must be after checkInDate");
        }

        DateRange dateRange = new DateRange(checkInDate, checkOutDate);
        
        List<Room> availableRooms = roomCatalog.searchAvailable(dateRange);
        
        if (roomType != null) {
            availableRooms = availableRooms.stream()
                    .filter(room -> room.getRoomType() == roomType)
                    .collect(Collectors.toList());
        }

        return availableRooms;
    }

    public List<Room> getAvailableRooms(DateRange dateRange) {
        return roomCatalog.searchAvailable(dateRange);
    }

    public void updateRoomStatus(int roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        switch (status) {
            case AVAILABLE:
                room.markAvailable();
                break;
            case OCCUPIED:
                room.markOccupied();
                break;
            case MAINTENANCE:
                room.markUnderMaintenance();
                break;
        }

        roomRepository.save(room);
    }

    public Room assignRoom(int bookingId, int roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room is not available");
        }

        room.markOccupied();
        roomRepository.save(room);
        return room;
    }

    public RoomComponent getRoomCatalog() {
        return roomCatalog;
    }
}

