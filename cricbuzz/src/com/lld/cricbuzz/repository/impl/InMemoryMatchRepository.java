package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.match.Match;
import com.lld.cricbuzz.repository.MatchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory repository for matches
 * Uses ConcurrentHashMap for storage
 * 
 * Note: Locking is handled at the service layer via LockManager.
 * This repository focuses only on data access.
 */
public class InMemoryMatchRepository implements MatchRepository {
    private final Map<String, Match> matches = new ConcurrentHashMap<>();

    @Override
    public void save(Match match) {
        matches.put(match.getMatchId(), match);
    }

    @Override
    public Optional<Match> findById(String matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    @Override
    public List<Match> findByTournamentId(String tournamentId) {
        return matches.values().stream()
            .filter(m -> m.getTournamentId().equals(tournamentId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Match> findByTeamId(String teamId) {
        return matches.values().stream()
            .filter(m -> m.getTeam1Id().equals(teamId) || m.getTeam2Id().equals(teamId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Match> findAll() {
        return new ArrayList<>(matches.values());
    }

    @Override
    public void delete(String matchId) {
        matches.remove(matchId);
    }
}

