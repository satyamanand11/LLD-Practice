package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.tournament.Tournament;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tournament entity
 * 
 * Even though Tournament is an aggregate root, it has its own repository
 * following real-world patterns where entities have separate repositories
 * 
 * Note: Locking is handled at the service layer via LockManager.
 * Repositories remain simple data access layers.
 */
public interface TournamentRepository {
    void save(Tournament tournament);
    Optional<Tournament> findById(String tournamentId);
    List<Tournament> findAll();
    void delete(String tournamentId);
}

