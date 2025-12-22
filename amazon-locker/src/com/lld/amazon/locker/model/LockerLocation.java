package com.lld.amazon.locker.model;

import java.util.*;

public class LockerLocation {

    private final String locationId;
    private final String name;
    private final OperatingHours operatingHours;

    // A location contains multiple lockers (R3)
    private final List<String> lockerIds;

    public LockerLocation(String locationId, String name, OperatingHours operatingHours, List<String> lockerIds) {
        this.locationId = locationId;
        this.name = name;
        this.operatingHours = operatingHours;
        this.lockerIds = List.copyOf(lockerIds);
    }

    public String getLocationId() { return locationId; }
    public String getName() { return name; }
    public OperatingHours getOperatingHours() { return operatingHours; }
    public List<String> getLockerIds() { return lockerIds; }
}
