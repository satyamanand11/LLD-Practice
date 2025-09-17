package com.demo.builders;

import com.demo.dto.ParkingLot;
import com.demo.dto.EntrancePanel;
import com.demo.dto.ExitPanel;
import com.demo.dto.parkingSpot.ParkingSpot;
import com.demo.enums.ParkingSpotEnum;

import java.util.ArrayList;
import java.util.List;

public class ParkingLotBuilder {
    private String name;
    private List<EntrancePanel> entrances = new ArrayList<>();
    private List<ExitPanel> exits = new ArrayList<>();
    private int miniSpots = 0;
    private int compactSpots = 0;
    private int largeSpots = 0;

    public ParkingLotBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ParkingLotBuilder addEntrance(EntrancePanel entrance) {
        this.entrances.add(entrance);
        return this;
    }

    public ParkingLotBuilder addExit(ExitPanel exit) {
        this.exits.add(exit);
        return this;
    }

    public ParkingLotBuilder withMiniSpots(int count) {
        this.miniSpots = count;
        return this;
    }

    public ParkingLotBuilder withCompactSpots(int count) {
        this.compactSpots = count;
        return this;
    }

    public ParkingLotBuilder withLargeSpots(int count) {
        this.largeSpots = count;
        return this;
    }

    public ParkingLot build() {
        ParkingLot parkingLot = new ParkingLot(name);
        parkingLot.setEntrances(entrances);
        parkingLot.setExits(exits);
        
        // Add parking spots based on configuration
        // This would typically use a factory to create spots
        return parkingLot;
    }
}
