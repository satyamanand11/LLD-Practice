package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.player.PlayerMatchStats;
import com.lld.cricbuzz.repository.PlayerMatchStatsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe repository for player match statistics
 * Uses ConcurrentHashMap for storage
 * 
 * Note: Locking is handled at the service layer via LockManager.
 * This repository focuses only on data access.
 */
public class InMemoryPlayerMatchStatsRepository implements PlayerMatchStatsRepository {
    private final Map<String, PlayerMatchStats> stats = new ConcurrentHashMap<>(); // key: playerId_matchId
    private final Map<String, List<String>> matchStats = new ConcurrentHashMap<>(); // matchId -> keys
    private final Map<String, List<String>> playerStats = new ConcurrentHashMap<>(); // playerId -> keys

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

