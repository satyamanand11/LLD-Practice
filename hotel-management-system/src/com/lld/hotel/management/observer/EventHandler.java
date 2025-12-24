package com.lld.hotel.management.observer;

public interface EventHandler<T extends DomainEvent> {

    Class<T> eventType();

    void handle(T event);

    default void tryHandle(DomainEvent event) {
        if (eventType().isInstance(event)) {
            handle(eventType().cast(event));
        }
    }
}
