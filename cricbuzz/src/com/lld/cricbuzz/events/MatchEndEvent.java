package com.lld.cricbuzz.events;

/**
 * Event published when a match ends
 */
public class MatchEndEvent extends MatchEvent {
    private final String winnerTeamId;

    public MatchEndEvent(String eventId, String matchId, String winnerTeamId) {
        super(eventId, matchId);
        this.winnerTeamId = winnerTeamId;
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }
}

