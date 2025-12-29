package com.lld.cricbuzz.strategy;

import com.lld.cricbuzz.domain.match.BallOutcome;
import com.lld.cricbuzz.domain.tournament.MatchFormat;

/**
 * Strategy for Test matches (unlimited overs, declarations, follow-on)
 */
public class TestScoringStrategy implements ScoringStrategy {
    @Override
    public MatchFormat getFormat() {
        return MatchFormat.TEST;
    }

    @Override
    public boolean isValidBall(BallOutcome outcome) {
        return true;
    }

    @Override
    public boolean shouldCompleteInnings(int overs, int wickets, int runs) {
        // Test matches can be declared or all out
        return wickets >= 10;
    }

    @Override
    public int getMaxOvers() {
        return -1; // Unlimited
    }
}

