package com.lld.cricbuzz.domain.match;

/**
 * Enum representing match lifecycle status
 */
public enum MatchStatus {
    SCHEDULED,
    TOSS_PENDING,
    LIVE,
    INNINGS_BREAK,
    COMPLETED,
    ABANDONED,
    CANCELLED
}

