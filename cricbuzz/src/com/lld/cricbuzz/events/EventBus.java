package com.lld.cricbuzz.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Bus for publishing and subscribing to domain events
 * Observer Pattern implementation
 */
public class EventBus {
    private static final EventBus instance = new EventBus();
    private final List<EventHandler> handlers = new CopyOnWriteArrayList<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    public void subscribe(EventHandler handler) {
        handlers.add(handler);
    }

    public void unsubscribe(EventHandler handler) {
        handlers.remove(handler);
    }

    public void publish(DomainEvent event) {
        for (EventHandler handler : handlers) {
            if (handler.canHandle(event)) {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    // Log error but don't fail the event publishing
                    System.err.println("Error handling event: " + e.getMessage());
                }
            }
        }
    }
}

