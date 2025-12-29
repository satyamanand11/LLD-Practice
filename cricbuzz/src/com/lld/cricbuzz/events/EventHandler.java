package com.lld.cricbuzz.events;

/**
 * Interface for event handlers
 * Observer Pattern
 */
public interface EventHandler {
    boolean canHandle(DomainEvent event);
    void handle(DomainEvent event);
}

