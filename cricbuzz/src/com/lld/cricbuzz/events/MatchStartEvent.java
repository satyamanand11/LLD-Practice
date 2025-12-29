package com.lld.cricbuzz.events;

/**
 * Event published when a match starts
 */
public class MatchStartEvent extends MatchEvent {
    public MatchStartEvent(String eventId, String matchId) {
        super(eventId, matchId);
    }
}

