package com.lld.ticketservice.managers.event;

import com.lld.ticketservice.domain.event.Event;

import java.util.List;

public interface EventManager {
    void addEvent(Event e);

    Event getEvent(int id);

    List<Event> listAll();
}
