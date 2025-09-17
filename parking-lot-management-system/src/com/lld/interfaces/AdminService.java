package com.demo.interfaces;

import com.demo.dto.accounts.Admin;
import com.demo.dto.parkingSpot.ParkingSpot;
import com.demo.enums.ParkingSpotEnum;

public interface AdminService {
    void addParkingSpot(ParkingSpotEnum spotType, int floor, int amount);
    void removeParkingSpot(int spotId);
    void updateParkingSpot(ParkingSpot spot);
    void generateReport();
}
