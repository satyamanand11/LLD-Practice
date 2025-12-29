package com.lld.cricbuzz.domain.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing an innings in a match
 */
public class Innings {
    private final String inningsId;
    private final String matchId;
    private final int inningsNumber;
    private final String battingTeamId;
    private final String bowlingTeamId;
    private final List<Over> overs;
    private final Map<String, String> currentBatters; // strikerId -> nonStrikerId
    private final Object lock = new Object(); // Lock for thread-safe operations
    private String currentBowlerId;
    private int totalRuns;
    private int totalWickets;
    private int totalExtras;
    private boolean isComplete;
    private boolean isDeclared; // For Test matches
    private boolean isFollowOn;

    public Innings(String inningsId, String matchId, int inningsNumber,
                   String battingTeamId, String bowlingTeamId) {
        if (inningsId == null || matchId == null || battingTeamId == null || bowlingTeamId == null) {
            throw new IllegalArgumentException("All required fields must be provided");
        }
        this.inningsId = inningsId;
        this.matchId = matchId;
        this.inningsNumber = inningsNumber;
        this.battingTeamId = battingTeamId;
        this.bowlingTeamId = bowlingTeamId;
        this.overs = new ArrayList<>();
        this.currentBatters = new HashMap<>();
        this.totalRuns = 0;
        this.totalWickets = 0;
        this.totalExtras = 0;
        this.isComplete = false;
        this.isDeclared = false;
        this.isFollowOn = false;
    }

    public void addOver(Over over) {
        synchronized (lock) {
            if (isComplete) {
                throw new IllegalStateException("Innings is already complete");
            }
            if (!over.getInningsId().equals(this.inningsId)) {
                throw new IllegalArgumentException("Over innings ID mismatch");
            }
            overs.add(over);
            updateStats(over);
        }
    }

    private void updateStats(Over over) {
        synchronized (lock) {
            this.totalRuns += over.getRuns();
            this.totalWickets += over.getWickets();
            this.totalExtras += over.getExtras();
        }
    }

    public void setCurrentBatters(String strikerId, String nonStrikerId) {
        if (strikerId == null || nonStrikerId == null) {
            throw new IllegalArgumentException("Both batter IDs are required");
        }
        this.currentBatters.clear();
        this.currentBatters.put(strikerId, nonStrikerId);
    }

    public void rotateStrike() {
        synchronized (lock) {
            // Swap striker and non-striker
            Map<String, String> newBatters = new HashMap<>();
            for (Map.Entry<String, String> entry : currentBatters.entrySet()) {
                newBatters.put(entry.getValue(), entry.getKey());
            }
            this.currentBatters.clear();
            this.currentBatters.putAll(newBatters);
        }
    }

    public void setCurrentBowler(String bowlerId) {
        if (bowlerId == null || bowlerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bowler ID cannot be null or empty");
        }
        this.currentBowlerId = bowlerId;
    }

    public void complete() {
        this.isComplete = true;
    }

    public void declare() {
        if (isComplete) {
            throw new IllegalStateException("Innings is already complete");
        }
        this.isDeclared = true;
        this.isComplete = true;
    }

    public void setFollowOn(boolean followOn) {
        this.isFollowOn = followOn;
    }

    public int getCurrentOverNumber() {
        return overs.size();
    }

    public int getTotalBalls() {
        return overs.stream()
            .mapToInt(o -> o.getBalls().size())
            .sum();
    }

    // Getters
    public String getInningsId() {
        return inningsId;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getInningsNumber() {
        return inningsNumber;
    }

    public String getBattingTeamId() {
        return battingTeamId;
    }

    public String getBowlingTeamId() {
        return bowlingTeamId;
    }

    public List<Over> getOvers() {
        return new ArrayList<>(overs);
    }

    public String getCurrentBowlerId() {
        return currentBowlerId;
    }

    public Map<String, String> getCurrentBatters() {
        return new HashMap<>(currentBatters);
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public int getTotalWickets() {
        return totalWickets;
    }

    public int getTotalExtras() {
        return totalExtras;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isDeclared() {
        return isDeclared;
    }

    public boolean isFollowOn() {
        return isFollowOn;
    }
}

