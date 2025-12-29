package com.lld.cricbuzz.domain.match;

import java.time.LocalDateTime;

/**
 * Entity representing a single ball event in a match
 */
public class BallEvent {
    private final String ballEventId;
    private final String inningsId;
    private final int overNumber;
    private final int ballNumber;
    private final String bowlerId;
    private final String strikerId;
    private final String nonStrikerId;
    private final BallOutcome outcome;
    private final LocalDateTime timestamp;
    private String commentary;

    public BallEvent(String ballEventId, String inningsId, int overNumber, int ballNumber,
                     String bowlerId, String strikerId, String nonStrikerId, BallOutcome outcome) {
        if (ballEventId == null || inningsId == null || bowlerId == null ||
            strikerId == null || nonStrikerId == null || outcome == null) {
            throw new IllegalArgumentException("All required fields must be provided");
        }
        if (overNumber < 0 || ballNumber < 1 || ballNumber > 6) {
            throw new IllegalArgumentException("Invalid over or ball number");
        }
        this.ballEventId = ballEventId;
        this.inningsId = inningsId;
        this.overNumber = overNumber;
        this.ballNumber = ballNumber;
        this.bowlerId = bowlerId;
        this.strikerId = strikerId;
        this.nonStrikerId = nonStrikerId;
        this.outcome = outcome;
        this.timestamp = LocalDateTime.now();
    }

    public void addCommentary(String commentary) {
        this.commentary = commentary;
    }

    // Getters
    public String getBallEventId() {
        return ballEventId;
    }

    public String getInningsId() {
        return inningsId;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public String getStrikerId() {
        return strikerId;
    }

    public String getNonStrikerId() {
        return nonStrikerId;
    }

    public BallOutcome getOutcome() {
        return outcome;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCommentary() {
        return commentary;
    }
}

