package com.lld.cricbuzz.domain.tournament;

/**
 * Enum representing different cricket match formats
 */
public enum MatchFormat {
    ODI("One Day International", 50),
    TEST("Test Match", -1), // Unlimited overs
    T20("Twenty20", 20);

    private final String displayName;
    private final int maxOvers;

    MatchFormat(String displayName, int maxOvers) {
        this.displayName = displayName;
        this.maxOvers = maxOvers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxOvers() {
        return maxOvers;
    }

    public boolean isLimitedOvers() {
        return maxOvers > 0;
    }
}

