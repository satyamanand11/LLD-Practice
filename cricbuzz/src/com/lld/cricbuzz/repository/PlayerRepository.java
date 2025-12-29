package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Player aggregate
 */
public interface PlayerRepository {
    void save(Player player);
    Optional<Player> findById(String playerId);
    List<Player> findByTeamId(String teamId);
    List<Player> findAll();
    void delete(String playerId);
}

