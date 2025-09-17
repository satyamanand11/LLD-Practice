package com.demo.services;

import com.demo.dto.*;
import com.demo.enums.*;
import com.demo.interfaces.*;

public class DisplayServiceImpl implements DisplayService , Observer {


    @Override
    public void update(ParkingEvent event) {
        if (event == null) {
            return;
        }
        
        int change = event.getEventType().equals(ParkingEventType.EnTRY) ? -1 : 1;
        DisplayBoard.getInstance().updateFreeSpots(event.getParkingSpotEnum(), change);
    }

    public void update(ParkingSpotEnum parkingSpotEnum, int change){
        if (parkingSpotEnum == null) {
            return;
        }
        DisplayBoard.getInstance().updateFreeSpots(parkingSpotEnum, change);
    }
}
