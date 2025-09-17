package com.demo.interfaces;

import com.demo.dto.parkingSpot.ParkingSpot;
import com.demo.enums.ParkingSpotEnum;

import java.util.List;
import java.util.Optional;

public interface ParkingSpotRepository {
    List<ParkingSpot> findFreeSpotsByType(ParkingSpotEnum spotType);
    List<ParkingSpot> findOccupiedSpotsByType(ParkingSpotEnum spotType);
    Optional<ParkingSpot> findById(int spotId);
    void save(ParkingSpot spot);
    void remove(ParkingSpot spot);
    void moveToFree(ParkingSpot spot);
    void moveToOccupied(ParkingSpot spot);
}
