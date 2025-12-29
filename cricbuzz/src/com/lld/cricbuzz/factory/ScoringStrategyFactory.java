package com.lld.cricbuzz.factory;

import com.lld.cricbuzz.domain.tournament.MatchFormat;
import com.lld.cricbuzz.strategy.ODIScoringStrategy;
import com.lld.cricbuzz.strategy.ScoringStrategy;
import com.lld.cricbuzz.strategy.T20ScoringStrategy;
import com.lld.cricbuzz.strategy.TestScoringStrategy;

/**
 * Factory Pattern for creating scoring strategies
 */
public class ScoringStrategyFactory {
    public static ScoringStrategy createStrategy(MatchFormat format) {
        return switch (format) {
            case ODI -> new ODIScoringStrategy();
            case T20 -> new T20ScoringStrategy();
            case TEST -> new TestScoringStrategy();
        };
    }
}

