package com.lld.cricbuzz.strategy;

import com.lld.cricbuzz.domain.match.BallOutcome;
import com.lld.cricbuzz.domain.tournament.MatchFormat;

/**
 * Strategy for T20 matches (20 overs)
 */
public class T20ScoringStrategy implements ScoringStrategy {
    @Override
    public MatchFormat getFormat() {
        return MatchFormat.T20;
    }

    @Override
    public boolean isValidBall(BallOutcome outcome) {
        return true;
    }

    @Override
    public boolean shouldCompleteInnings(int overs, int wickets, int runs) {
        return overs >= 20 || wickets >= 10;
    }

    @Override
    public int getMaxOvers() {
        return 20;
    }
}

