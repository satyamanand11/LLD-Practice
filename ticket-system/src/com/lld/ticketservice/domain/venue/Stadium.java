package com.lld.ticketservice.domain.venue;

public class Stadium extends Venue {
    public Stadium(int id, String name, String address) {
        super(id, name, address, VenueType.STADIUM);
    }
}
