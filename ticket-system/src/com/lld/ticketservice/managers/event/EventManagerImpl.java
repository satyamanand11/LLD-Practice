package com.lld.ticketservice.managers.event;

import com.lld.ticketservice.domain.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManagerImpl implements EventManager {
    private final Map<Integer, Event> store = new ConcurrentHashMap<>();

    public void addEvent(Event e) {
        store.put(e.getId(), e);
    }

    public Event getEvent(int id) {
        return store.get(id);
    }

    public List<Event> listAll() {
        return new ArrayList<>(store.values());
    }
}
