package com.lld.hotel.management.entities;

public class Room {

    private final int roomId;
    private final String roomNumber;
    private final RoomType roomType;
    private RoomStatus status;

    public Room(int roomId, String roomNumber, RoomType roomType) {

        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("roomNumber is required");
        }
        if (roomType == null) {
            throw new IllegalArgumentException("roomType is required");
        }

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = RoomStatus.AVAILABLE;
    }

    public void markOccupied() {
        if (status == RoomStatus.MAINTENANCE) {
            throw new IllegalStateException(
                    "Room under maintenance cannot be occupied"
            );
        }
        this.status = RoomStatus.OCCUPIED;
    }

    public void markAvailable() {
        this.status = RoomStatus.AVAILABLE;
    }

    public void markUnderMaintenance() {
        this.status = RoomStatus.MAINTENANCE;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public RoomStatus getStatus() {
        return status;
    }
}
