package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.IdGenerator;
import com.lld.hotel.management.entities.RoomKey;
import com.lld.hotel.management.entities.RoomKeyType;
import com.lld.hotel.management.repository.RoomKeyRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * KeyManagementService (R9)
 * Manages room keys and master keys
 */
public class KeyManagementService {
    private final RoomKeyRepository keyRepository;

    public KeyManagementService(RoomKeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    public RoomKey generateRoomKey(int roomId) {
        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }

        Set<Integer> roomIds = new HashSet<>();
        roomIds.add(roomId);

        RoomKey key = new RoomKey(
                IdGenerator.nextId(),
                RoomKeyType.ROOM_KEY,
                roomIds
        );

        keyRepository.save(key);
        return key;
    }

    public RoomKey generateMasterKey(Set<Integer> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            throw new IllegalArgumentException("roomIds cannot be empty");
        }

        RoomKey key = new RoomKey(
                IdGenerator.nextId(),
                RoomKeyType.MASTER_KEY,
                new HashSet<>(roomIds)
        );

        keyRepository.save(key);
        return key;
    }

    public boolean validateKey(int keyId, int roomId) {
        if (keyId <= 0 || roomId <= 0) {
            return false;
        }

        RoomKey key = keyRepository.findById(keyId)
                .orElse(null);

        if (key == null || !key.isActive()) {
            return false;
        }

        return key.canAccessRoom(roomId);
    }

    public void deactivateKey(int keyId) {
        RoomKey key = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Key not found"));

        key.deactivate();
        keyRepository.save(key);
    }

    public RoomKey getKey(int keyId) {
        return keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Key not found"));
    }
}

