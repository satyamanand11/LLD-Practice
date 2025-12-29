package com.lld.cricbuzz.domain.match;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an over (6 balls)
 */
public class Over {
    private final String overId;
    private final String inningsId;
    private final int overNumber;
    private final String bowlerId;
    private final List<BallEvent> balls;
    private boolean isComplete;
    private int runs;
    private int wickets;
    private int extras;
    private final Object lock = new Object(); // Lock for thread-safe operations

    public Over(String overId, String inningsId, int overNumber, String bowlerId) {
        if (overId == null || inningsId == null || bowlerId == null) {
            throw new IllegalArgumentException("Over ID, innings ID, and bowler ID are required");
        }
        if (overNumber < 0) {
            throw new IllegalArgumentException("Over number cannot be negative");
        }
        this.overId = overId;
        this.inningsId = inningsId;
        this.overNumber = overNumber;
        this.bowlerId = bowlerId;
        this.balls = new ArrayList<>();
        this.isComplete = false;
        this.runs = 0;
        this.wickets = 0;
        this.extras = 0;
    }

    public void addBall(BallEvent ballEvent) {
        synchronized (lock) {
            if (isComplete) {
                throw new IllegalStateException("Over is already complete");
            }
            if (ballEvent.getOverNumber() != this.overNumber) {
                throw new IllegalArgumentException("Ball event over number mismatch");
            }
            if (balls.size() >= 6 && !ballEvent.getOutcome().isExtra()) {
                throw new IllegalStateException("Over already has 6 valid balls");
            }
            
            balls.add(ballEvent);
            updateStats(ballEvent);
            
            // Over is complete when we have 6 valid balls (excluding extras like wides/no-balls that don't count)
            int validBalls = (int) balls.stream()
                .filter(b -> !b.getOutcome().isExtra() || 
                        (b.getOutcome().getExtraType() != ExtraType.WIDE && 
                         b.getOutcome().getExtraType() != ExtraType.NO_BALL))
                .count();
            
            if (validBalls >= 6) {
                this.isComplete = true;
            }
        }
    }

    private void updateStats(BallEvent ballEvent) {
        BallOutcome outcome = ballEvent.getOutcome();
        this.runs += outcome.getRuns();
        if (outcome.isWicket()) {
            this.wickets++;
        }
        if (outcome.isExtra()) {
            this.extras += outcome.getRuns();
        }
    }

    public boolean isMaiden() {
        return runs == 0 && wickets == 0;
    }

    // Getters
    public String getOverId() {
        return overId;
    }

    public String getInningsId() {
        return inningsId;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public List<BallEvent> getBalls() {
        return new ArrayList<>(balls);
    }

    public boolean isComplete() {
        return isComplete;
    }

    public int getRuns() {
        return runs;
    }

    public int getWickets() {
        return wickets;
    }

    public int getExtras() {
        return extras;
    }
}

