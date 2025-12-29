package com.lld.cricbuzz.events;

/**
 * Event published when a boundary (4 or 6) is scored
 */
public class BoundaryEvent extends MatchEvent {
    private final String batterId;
    private final int runs;
    private final boolean isSix;

    public BoundaryEvent(String eventId, String matchId, String batterId, int runs) {
        super(eventId, matchId);
        this.batterId = batterId;
        this.runs = runs;
        this.isSix = (runs == 6);
    }

    public String getBatterId() {
        return batterId;
    }

    public int getRuns() {
        return runs;
    }

    public boolean isSix() {
        return isSix;
    }
}

