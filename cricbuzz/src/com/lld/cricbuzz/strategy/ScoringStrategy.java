package com.lld.cricbuzz.strategy;

import com.lld.cricbuzz.domain.match.BallOutcome;
import com.lld.cricbuzz.domain.tournament.MatchFormat;

/**
 * Strategy Pattern for different scoring rules based on match format
 */
public interface ScoringStrategy {
    MatchFormat getFormat();
    boolean isValidBall(BallOutcome outcome);
    boolean shouldCompleteInnings(int overs, int wickets, int runs);
    int getMaxOvers();
}

