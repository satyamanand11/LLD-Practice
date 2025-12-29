package com.lld.cricbuzz.domain.match;

/**
 * Enum representing types of runs scored
 */
public enum RunType {
    REGULAR(0, false),
    FOUR(4, false),
    SIX(6, false),
    BYE(0, true),
    LEG_BYE(0, true),
    NO_BALL(0, true),
    WIDE(0, true);

    private final int runs;
    private final boolean isExtra;

    RunType(int runs, boolean isExtra) {
        this.runs = runs;
        this.isExtra = isExtra;
    }

    public int getRuns() {
        return runs;
    }

    public boolean isExtra() {
        return isExtra;
    }
}

