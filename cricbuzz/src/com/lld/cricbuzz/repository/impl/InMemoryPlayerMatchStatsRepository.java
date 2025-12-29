package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.player.PlayerMatchStats;
import com.lld.cricbuzz.repository.PlayerMatchStatsRepository;

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
 * Thread-safe repository for player match statistics
 * Uses ReentrantLock with timeout for fine-grained locking per player-match combination
 */
public class InMemoryPlayerMatchStatsRepository implements PlayerMatchStatsRepository {
    private final Map<String, PlayerMatchStats> stats = new ConcurrentHashMap<>(); // key: playerId_matchId
    private final Map<String, List<String>> matchStats = new ConcurrentHashMap<>(); // matchId -> keys
    private final Map<String, List<String>> playerStats = new ConcurrentHashMap<>(); // playerId -> keys
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>(); // Per player-match lock
    private static final int LOCK_TIMEOUT_SECONDS = 5;

    private String getKey(String playerId, String matchId) {
        return playerId + "_" + matchId;
    }

    @Override
    public void save(PlayerMatchStats stats) {
        String key = getKey(stats.getPlayerId(), stats.getMatchId());
        this.stats.put(key, stats);
        matchStats.computeIfAbsent(stats.getMatchId(), k -> new ArrayList<>()).add(key);
        playerStats.computeIfAbsent(stats.getPlayerId(), k -> new ArrayList<>()).add(key);
    }

    /**
     * Execute an operation with player-match level lock
     * Uses ReentrantLock with timeout to prevent deadlocks
     * Ensures atomic updates to player statistics
     */
    public void executeWithLock(String playerId, String matchId, Consumer<PlayerMatchStats> action) {
        String key = getKey(playerId, matchId);
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock(false));
        
        try {
            if (lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    PlayerMatchStats playerStats = stats.computeIfAbsent(key, 
                        k -> new PlayerMatchStats(playerId, matchId));
                    action.accept(playerStats);
                    stats.put(key, playerStats);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException(
                    "Could not acquire lock for player stats: " + key + " within " + 
                    LOCK_TIMEOUT_SECONDS + " seconds. Possible deadlock or high contention.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lock acquisition interrupted for player stats: " + key, e);
        }
    }

    @Override
    public Optional<PlayerMatchStats> findByPlayerIdAndMatchId(String playerId, String matchId) {
        String key = getKey(playerId, matchId);
        return Optional.ofNullable(stats.get(key));
    }

    @Override
    public List<PlayerMatchStats> findByMatchId(String matchId) {
        List<String> keys = matchStats.getOrDefault(matchId, new ArrayList<>());
        return keys.stream()
            .map(stats::get)
            .filter(s -> s != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<PlayerMatchStats> findByPlayerId(String playerId) {
        List<String> keys = playerStats.getOrDefault(playerId, new ArrayList<>());
        return keys.stream()
            .map(stats::get)
            .filter(s -> s != null)
            .collect(Collectors.toList());
    }
}

