package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.RoomAvailability;
import java.util.function.Consumer;

public interface RoomAvailabilityRepository {

    void executeWithLock(
            int roomId,
            Consumer<RoomAvailability> action
    );
}
