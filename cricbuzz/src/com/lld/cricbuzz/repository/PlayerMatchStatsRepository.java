package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.player.PlayerMatchStats;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PlayerMatchStats
 */
public interface PlayerMatchStatsRepository {
    void save(PlayerMatchStats stats);
    Optional<PlayerMatchStats> findByPlayerIdAndMatchId(String playerId, String matchId);
    List<PlayerMatchStats> findByMatchId(String matchId);
    List<PlayerMatchStats> findByPlayerId(String playerId);
}

