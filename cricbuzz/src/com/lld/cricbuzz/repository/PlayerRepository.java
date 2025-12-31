package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Player entity
 * 
 * In real-world systems, entities often have separate repositories
 * even if they're part of an aggregate. This allows:
 * - Independent querying
 * - Better performance optimization
 * - Clearer separation of concerns
 * - Easier to scale/migrate to microservices
 * 
 * Note: Locking is handled at the service layer via LockManager.
 * Repositories remain simple data access layers.
 */
public interface PlayerRepository {
    void save(Player player);
    Optional<Player> findById(String playerId);
    List<Player> findByTeamId(String teamId);
    List<Player> findAll();
    void delete(String playerId);
}

