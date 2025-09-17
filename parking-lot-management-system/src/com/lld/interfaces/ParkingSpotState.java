package com.demo.interfaces;

import com.demo.dto.vehicle.Vehicle;

public interface ParkingSpotState {
    void parkVehicle(Vehicle vehicle);
    void freeSpot();
    boolean isAvailable();
    String getStateName();
}
