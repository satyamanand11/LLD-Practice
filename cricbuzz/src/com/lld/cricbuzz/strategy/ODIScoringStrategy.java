package com.lld.cricbuzz.strategy;

import com.lld.cricbuzz.domain.match.BallOutcome;
import com.lld.cricbuzz.domain.tournament.MatchFormat;

/**
 * Strategy for ODI matches (50 overs)
 */
public class ODIScoringStrategy implements ScoringStrategy {
    @Override
    public MatchFormat getFormat() {
        return MatchFormat.ODI;
    }

    @Override
    public boolean isValidBall(BallOutcome outcome) {
        return true; // All balls are valid in ODI
    }

    @Override
    public boolean shouldCompleteInnings(int overs, int wickets, int runs) {
        return overs >= 50 || wickets >= 10;
    }

    @Override
    public int getMaxOvers() {
        return 50;
    }
}

