package com.demo.services;

import com.demo.dto.ParkingLot;
import com.demo.dto.parkingSpot.ParkingSpot;
import com.demo.enums.ParkingSpotEnum;
import com.demo.interfaces.ParkingSpotRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParkingSpotRepositoryImpl implements ParkingSpotRepository {
    private final ParkingLot parkingLot;

    public ParkingSpotRepositoryImpl() {
        this.parkingLot = ParkingLot.getInstance();
    }

    @Override
    public List<ParkingSpot> findFreeSpotsByType(ParkingSpotEnum spotType) {
        return parkingLot.getFreeParkingSpots().get(spotType);
    }

    @Override
    public List<ParkingSpot> findOccupiedSpotsByType(ParkingSpotEnum spotType) {
        return parkingLot.getOccupiedParkingSpots().get(spotType);
    }

    @Override
    public Optional<ParkingSpot> findById(int spotId) {
        return parkingLot.getFreeParkingSpots().values().stream()
                .flatMap(List::stream)
                .filter(spot -> spot.getId() == spotId)
                .findFirst()
                .or(() -> parkingLot.getOccupiedParkingSpots().values().stream()
                        .flatMap(List::stream)
                        .filter(spot -> spot.getId() == spotId)
                        .findFirst());
    }

    @Override
    public void save(ParkingSpot spot) {
        // This would typically save to a database
        // For now, we'll add to free spots
        parkingLot.getFreeParkingSpots().get(spot.getParkingSpotEnum()).add(spot);
    }

    @Override
    public void remove(ParkingSpot spot) {
        parkingLot.getFreeParkingSpots().get(spot.getParkingSpotEnum()).remove(spot);
        parkingLot.getOccupiedParkingSpots().get(spot.getParkingSpotEnum()).remove(spot);
    }

    @Override
    public void moveToFree(ParkingSpot spot) {
        parkingLot.getOccupiedParkingSpots().get(spot.getParkingSpotEnum()).remove(spot);
        parkingLot.getFreeParkingSpots().get(spot.getParkingSpotEnum()).add(spot);
    }

    @Override
    public void moveToOccupied(ParkingSpot spot) {
        parkingLot.getFreeParkingSpots().get(spot.getParkingSpotEnum()).remove(spot);
        parkingLot.getOccupiedParkingSpots().get(spot.getParkingSpotEnum()).add(spot);
    }
}
