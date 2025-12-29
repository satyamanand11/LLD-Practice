package com.lld.cricbuzz.events;

/**
 * Event published when a player reaches a milestone
 */
public class MilestoneEvent extends MatchEvent {
    private final String playerId;
    private final MilestoneType type;
    private final int value;

    public MilestoneEvent(String eventId, String matchId, String playerId, MilestoneType type, int value) {
        super(eventId, matchId);
        this.playerId = playerId;
        this.type = type;
        this.value = value;
    }

    public String getPlayerId() {
        return playerId;
    }

    public MilestoneType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public enum MilestoneType {
        FIFTY,
        HUNDRED,
        HUNDRED_FIFTY,
        DOUBLE_HUNDRED,
        FIVE_WICKET_HAUL,
        HAT_TRICK
    }
}

