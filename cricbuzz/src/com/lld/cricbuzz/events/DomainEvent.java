package com.lld.cricbuzz.events;

import java.time.LocalDateTime;

/**
 * Base interface for domain events
 * Observer Pattern implementation
 */
public interface DomainEvent {
    String getEventId();
    LocalDateTime getTimestamp();
    String getMatchId();
}

