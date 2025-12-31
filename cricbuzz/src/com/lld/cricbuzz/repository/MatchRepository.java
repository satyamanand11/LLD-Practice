package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.match.Match;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Match entity
 * 
 * Match is both an aggregate root AND a frequently accessed entity.
 * In real systems, this dual nature is common - aggregate roots
 * still have their own repositories for independent access.
 * 
 * Note: Locking is handled at the service layer via LockManager.
 * Repositories remain simple data access layers.
 */
public interface MatchRepository {
    void save(Match match);
    Optional<Match> findById(String matchId);
    List<Match> findByTournamentId(String tournamentId);
    List<Match> findByTeamId(String teamId);
    List<Match> findAll();
    void delete(String matchId);
}

