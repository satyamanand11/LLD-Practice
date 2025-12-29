package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.match.Match;
import com.lld.cricbuzz.repository.MatchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory repository for matches
 * Uses ConcurrentHashMap for storage and ReentrantLock for fine-grained locking
 * with timeout protection to prevent deadlocks
 */
public class InMemoryMatchRepository implements MatchRepository {
    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static final int LOCK_TIMEOUT_SECONDS = 5;

    @Override
    public void save(Match match) {
        matches.put(match.getMatchId(), match);
    }

    /**
     * Execute an operation with match-level lock
     * Uses ReentrantLock with timeout to prevent deadlocks
     * Prevents race conditions when multiple threads update the same match
     */
    public void executeWithLock(String matchId, Consumer<Match> action) {
        ReentrantLock lock = locks.computeIfAbsent(matchId, k -> new ReentrantLock(false));
        
        try {
            if (lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    Match match = matches.get(matchId);
                    if (match == null) {
                        throw new IllegalArgumentException("Match not found: " + matchId);
                    }
                    action.accept(match);
                    matches.put(matchId, match);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException(
                    "Could not acquire lock for match: " + matchId + " within " + 
                    LOCK_TIMEOUT_SECONDS + " seconds. Possible deadlock or high contention.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lock acquisition interrupted for match: " + matchId, e);
        }
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

