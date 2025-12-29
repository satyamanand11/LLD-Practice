package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.tournament.Tournament;
import com.lld.cricbuzz.repository.TournamentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of TournamentRepository
 * Thread-safe implementation
 */
public class InMemoryTournamentRepository implements TournamentRepository {
    private final Map<String, Tournament> tournaments = new ConcurrentHashMap<>();

    @Override
    public void save(Tournament tournament) {
        tournaments.put(tournament.getTournamentId(), tournament);
    }

    @Override
    public Optional<Tournament> findById(String tournamentId) {
        return Optional.ofNullable(tournaments.get(tournamentId));
    }

    @Override
    public List<Tournament> findAll() {
        return new ArrayList<>(tournaments.values());
    }

    @Override
    public void delete(String tournamentId) {
        tournaments.remove(tournamentId);
    }
}

