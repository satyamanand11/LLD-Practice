package com.demo.interfaces;

import com.demo.dto.ParkingTicket;
import com.demo.dto.vehicle.Vehicle;

public interface AttendantService {
    ParkingTicket issueTicket(Vehicle vehicle);
    void validateTicket(ParkingTicket ticket);
    void processPayment(ParkingTicket ticket);
}
