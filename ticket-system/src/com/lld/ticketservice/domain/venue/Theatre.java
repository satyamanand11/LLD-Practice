package com.lld.ticketservice.domain.venue;

public class Theatre extends Venue {
    public Theatre(int id, String name, String address) {
        super(id, name, address, VenueType.THEATRE);
    }
}
