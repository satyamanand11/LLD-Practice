package com.lld.hotel.management.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class RoomKey {

    private final int keyId;
    private final Set<Integer> roomIds = new HashSet<>();
    private final RoomKeyType keyType;

    private boolean active;

    public RoomKey(int keyId, RoomKeyType keyType, Set<Integer> roomIds) {

        if (keyId <= 0) {
            throw new IllegalArgumentException("keyId must be positive");
        }
        if (keyType == null) {
            throw new IllegalArgumentException("keyType is required");
        }
        if (roomIds == null || roomIds.isEmpty()) {
            throw new IllegalArgumentException("Key must be associated with at least one room");
        }

        if (keyType == RoomKeyType.ROOM_KEY && roomIds.size() != 1) {
            throw new IllegalArgumentException(
                    "ROOM_KEY must be associated with exactly one room"
            );
        }

        this.keyId = keyId;
        this.keyType = keyType;
        this.roomIds.addAll(roomIds);
        this.active = true;
    }

    public boolean canAccessRoom(int roomId) {
        if (!active) {
            return false;
        }
        return roomIds.contains(roomId);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public int getKeyId() {
        return keyId;
    }

    public RoomKeyType getKeyType() {
        return keyType;
    }

    public boolean isActive() {
        return active;
    }

    public Set<Integer> getRoomIds() {
        return Collections.unmodifiableSet(roomIds);
    }
}
