package com.lld.ticketservice.domain.event;

public abstract class Event {
    protected final int id;
    protected final String description;

    protected Event(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
