package com.demo.states;

import com.demo.dto.vehicle.Vehicle;
import com.demo.interfaces.ParkingSpotState;

public class FreeState implements ParkingSpotState {
    @Override
    public void parkVehicle(Vehicle vehicle) {
        // Transition to occupied state
        System.out.println("Vehicle " + vehicle.getId() + " parked successfully");
    }

    @Override
    public void freeSpot() {
        // Already free, no action needed
        System.out.println("Spot is already free");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getStateName() {
        return "FREE";
    }
}
