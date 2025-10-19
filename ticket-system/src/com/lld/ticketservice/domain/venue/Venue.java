package com.lld.ticketservice.domain.venue;

public abstract class Venue {
    protected final int id;
    protected final String name;
    protected final String address;
    protected final VenueType type;

    protected Venue(int id, String name, String address, VenueType type) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public VenueType getType() {
        return type;
    }
}