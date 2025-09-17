package com.demo.validators;

import com.demo.dto.vehicle.Vehicle;
import com.demo.dto.ParkingTicket;
import com.demo.exceptions.InvalidTicketException;

public class ParkingValidator {
    
    public static void validateVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        if (vehicle.getParkingSpotEnum() == null) {
            throw new IllegalArgumentException("Vehicle must have a valid parking spot type");
        }
    }
    
    public static void validateTicket(ParkingTicket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Parking ticket cannot be null");
        }
        if (ticket.getVehicle() == null) {
            throw new InvalidTicketException("Invalid ticket: missing vehicle information");
        }
        if (ticket.getParkingSpot() == null) {
            throw new InvalidTicketException("Invalid ticket: missing parking spot information");
        }
    }
    
    public static void validateAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
}
