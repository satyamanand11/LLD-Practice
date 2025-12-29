package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.match.Match;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Repository interface for Match aggregate
 */
public interface MatchRepository {
    void save(Match match);
    Optional<Match> findById(String matchId);
    List<Match> findByTournamentId(String tournamentId);
    List<Match> findByTeamId(String teamId);
    List<Match> findAll();
    void delete(String matchId);
    
    /**
     * Execute an operation with match-level lock for thread-safe updates
     * @param matchId The match ID to lock
     * @param action The operation to perform on the match
     */
    default void executeWithLock(String matchId, Consumer<Match> action) {
        // Default implementation - can be overridden by implementations
        Match match = findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        action.accept(match);
        save(match);
    }
}

