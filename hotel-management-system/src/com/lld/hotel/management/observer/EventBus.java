package com.lld.hotel.management.observer;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private final List<EventHandler<?>> handlers = new ArrayList<>();

    public void register(EventHandler<?> handler) {
        handlers.add(handler);
    }

    @SuppressWarnings("unchecked")
    public void publish(DomainEvent event) {
        for (EventHandler handler : handlers) {
            handler.tryHandle(event);
        }
    }
}
