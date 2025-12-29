package com.lld.cricbuzz.domain.player;

import java.util.ArrayList;
import java.util.List;

/**
 * Value Object representing player statistics for a specific match
 */
public class PlayerMatchStats {
    private final String playerId;
    private final String matchId;
    
    // Batting stats
    private volatile int runs;
    private volatile int ballsFaced;
    private volatile int fours;
    private volatile int sixes;
    private volatile boolean isOut;
    private volatile String dismissalType;
    
    // Bowling stats
    private volatile int oversBowled;
    private volatile int ballsBowled;
    private volatile int runsConceded;
    private volatile int wickets;
    private volatile int maidens;
    private final List<String> wicketTypes; // Thread-safe via external locking
    
    // Fielding stats
    private volatile int catches;
    private volatile int stumpings;
    private volatile int runOuts;

    public PlayerMatchStats(String playerId, String matchId) {
        if (playerId == null || matchId == null) {
            throw new IllegalArgumentException("Player ID and Match ID are required");
        }
        this.playerId = playerId;
        this.matchId = matchId;
        this.wicketTypes = new ArrayList<>();
    }

    // Batting methods
    public void addRuns(int runs) {
        if (runs < 0) {
            throw new IllegalArgumentException("Runs cannot be negative");
        }
        this.runs += runs;
        this.ballsFaced++;
    }

    public void addFour() {
        this.fours++;
        addRuns(4);
    }

    public void addSix() {
        this.sixes++;
        addRuns(6);
    }

    public void recordDismissal(String dismissalType) {
        this.isOut = true;
        this.dismissalType = dismissalType;
    }

    public void incrementBallsFaced() {
        this.ballsFaced++;
    }

    // Bowling methods
    public void addOver() {
        this.oversBowled++;
        this.ballsBowled += 6;
    }

    public void addBall() {
        this.ballsBowled++;
    }

    public void addRunsConceded(int runs) {
        if (runs < 0) {
            throw new IllegalArgumentException("Runs conceded cannot be negative");
        }
        this.runsConceded += runs;
    }

    public void addWicket(String wicketType) {
        this.wickets++;
        this.wicketTypes.add(wicketType);
    }

    public void addMaiden() {
        this.maidens++;
    }

    // Fielding methods
    public void addCatch() {
        this.catches++;
    }

    public void addStumping() {
        this.stumpings++;
    }

    public void addRunOut() {
        this.runOuts++;
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getRuns() {
        return runs;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public int getFours() {
        return fours;
    }

    public int getSixes() {
        return sixes;
    }

    public boolean isOut() {
        return isOut;
    }

    public String getDismissalType() {
        return dismissalType;
    }

    public int getOversBowled() {
        return oversBowled;
    }

    public int getBallsBowled() {
        return ballsBowled;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public int getWickets() {
        return wickets;
    }

    public int getMaidens() {
        return maidens;
    }

    public List<String> getWicketTypes() {
        return new ArrayList<>(wicketTypes);
    }

    public int getCatches() {
        return catches;
    }

    public int getStumpings() {
        return stumpings;
    }

    public int getRunOuts() {
        return runOuts;
    }

    public double getStrikeRate() {
        if (ballsFaced == 0) return 0.0;
        return (runs * 100.0) / ballsFaced;
    }

    public double getEconomyRate() {
        double overs = oversBowled + (ballsBowled % 6) / 6.0;
        if (overs == 0) return 0.0;
        return runsConceded / overs;
    }

    public double getBowlingAverage() {
        if (wickets == 0) return 0.0;
        return (double) runsConceded / wickets;
    }
}

