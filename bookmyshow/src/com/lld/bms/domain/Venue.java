package com.lld.bms.domain;

public class Venue {
    private final String id;
    private final String cityId;
    private final String name;
    private final String address;

    public Venue(String id, String cityId, String name, String address) {
        this.id = id;
        this.cityId = cityId;
        this.name = name;
        this.address = address;
    }

    public String getId() { return id; }
    public String getCityId() { return cityId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
}
