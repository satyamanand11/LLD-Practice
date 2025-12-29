package com.lld.cricbuzz.domain.match;

import java.util.Objects;

/**
 * Value Object representing the outcome of a ball
 */
public class BallOutcome {
    private final RunType runType;
    private final int runs;
    private final boolean isWicket;
    private final Wicket wicket;
    private final boolean isExtra;
    private final ExtraType extraType;

    private BallOutcome(RunType runType, int runs, boolean isWicket, Wicket wicket,
                       boolean isExtra, ExtraType extraType) {
        this.runType = Objects.requireNonNull(runType, "Run type cannot be null");
        this.runs = runs;
        this.isWicket = isWicket;
        this.wicket = wicket;
        this.isExtra = isExtra;
        this.extraType = extraType;
    }

    public static BallOutcome regularRuns(int runs) {
        if (runs < 0 || runs > 6 || runs == 5) {
            throw new IllegalArgumentException("Invalid runs: " + runs);
        }
        RunType runType = (runs == 4) ? RunType.FOUR : (runs == 6) ? RunType.SIX : RunType.REGULAR;
        return new BallOutcome(runType, runs, false, null, false, null);
    }

    public static BallOutcome wicket(Wicket wicket) {
        return new BallOutcome(RunType.REGULAR, 0, true, wicket, false, null);
    }

    public static BallOutcome extra(ExtraType extraType, int runs) {
        RunType runType = switch (extraType) {
            case BYE -> RunType.BYE;
            case LEG_BYE -> RunType.LEG_BYE;
            case NO_BALL -> RunType.NO_BALL;
            case WIDE -> RunType.WIDE;
            case PENALTY -> RunType.REGULAR;
        };
        return new BallOutcome(runType, runs, false, null, true, extraType);
    }

    public static BallOutcome wicketWithExtra(Wicket wicket, ExtraType extraType) {
        RunType runType = switch (extraType) {
            case NO_BALL -> RunType.NO_BALL;
            default -> RunType.REGULAR;
        };
        return new BallOutcome(runType, 0, true, wicket, true, extraType);
    }

    // Getters
    public RunType getRunType() {
        return runType;
    }

    public int getRuns() {
        return runs;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public Wicket getWicket() {
        return wicket;
    }

    public boolean isExtra() {
        return isExtra;
    }

    public ExtraType getExtraType() {
        return extraType;
    }
}

