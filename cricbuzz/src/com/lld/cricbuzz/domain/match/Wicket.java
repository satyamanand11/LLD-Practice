package com.lld.cricbuzz.domain.match;

/**
 * Value Object representing a wicket/dismissal
 */
public class Wicket {
    private final String wicketId;
    private final String batterId;
    private final WicketType type;
    private final String bowlerId;
    private final String fielderId; // For caught, run-out, stumped
    private final int overNumber;
    private final int ballNumber;
    private final int runsAtWicket;
    private final int ballsAtWicket;

    public Wicket(String wicketId, String batterId, WicketType type, String bowlerId,
                 String fielderId, int overNumber, int ballNumber, int runsAtWicket, int ballsAtWicket) {
        if (wicketId == null || batterId == null || type == null || bowlerId == null) {
            throw new IllegalArgumentException("Wicket ID, batter ID, type, and bowler ID are required");
        }
        this.wicketId = wicketId;
        this.batterId = batterId;
        this.type = type;
        this.bowlerId = bowlerId;
        this.fielderId = fielderId;
        this.overNumber = overNumber;
        this.ballNumber = ballNumber;
        this.runsAtWicket = runsAtWicket;
        this.ballsAtWicket = ballsAtWicket;
    }

    // Getters
    public String getWicketId() {
        return wicketId;
    }

    public String getBatterId() {
        return batterId;
    }

    public WicketType getType() {
        return type;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public String getFielderId() {
        return fielderId;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public int getRunsAtWicket() {
        return runsAtWicket;
    }

    public int getBallsAtWicket() {
        return ballsAtWicket;
    }
}

