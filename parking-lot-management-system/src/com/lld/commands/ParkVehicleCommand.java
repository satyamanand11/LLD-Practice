package com.demo.commands;

import com.demo.dto.ParkingTicket;
import com.demo.dto.parkingSpot.ParkingSpot;
import com.demo.dto.vehicle.Vehicle;
import com.demo.interfaces.Command;
import com.demo.interfaces.ParkingSpotRepository;

public class ParkVehicleCommand implements Command {
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private final ParkingSpotRepository repository;
    private ParkingTicket ticket;
    private boolean executed = false;

    public ParkVehicleCommand(Vehicle vehicle, ParkingSpot parkingSpot, ParkingSpotRepository repository) {
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.repository = repository;
    }

    @Override
    public void execute() {
        if (!executed && parkingSpot.isFree()) {
            synchronized (parkingSpot) {
                if (parkingSpot.isFree()) {
                    parkingSpot.setFree(false);
                    repository.moveToOccupied(parkingSpot);
                    ticket = new ParkingTicket(vehicle, parkingSpot);
                    executed = true;
                }
            }
        }
    }

    @Override
    public void undo() {
        if (executed) {
            parkingSpot.setFree(true);
            repository.moveToFree(parkingSpot);
            executed = false;
        }
    }

    public ParkingTicket getTicket() {
        return ticket;
    }
}
