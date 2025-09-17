package com.demo.states;

import com.demo.dto.vehicle.Vehicle;
import com.demo.interfaces.ParkingSpotState;

public class OccupiedState implements ParkingSpotState {
    @Override
    public void parkVehicle(Vehicle vehicle) {
        System.out.println("Cannot park vehicle - spot is already occupied");
    }

    @Override
    public void freeSpot() {
        // Transition to free state
        System.out.println("Spot freed successfully");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getStateName() {
        return "OCCUPIED";
    }
}
