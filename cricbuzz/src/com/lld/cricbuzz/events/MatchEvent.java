package com.lld.cricbuzz.events;

import java.time.LocalDateTime;

/**
 * Base class for match-related events
 */
public abstract class MatchEvent implements DomainEvent {
    private final String eventId;
    private final String matchId;
    private final LocalDateTime timestamp;

    protected MatchEvent(String eventId, String matchId) {
        this.eventId = eventId;
        this.matchId = matchId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getMatchId() {
        return matchId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

