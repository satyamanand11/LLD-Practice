package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.tournament.Tournament;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tournament aggregate
 * Follows Dependency Inversion Principle
 */
public interface TournamentRepository {
    void save(Tournament tournament);
    Optional<Tournament> findById(String tournamentId);
    List<Tournament> findAll();
    void delete(String tournamentId);
}

