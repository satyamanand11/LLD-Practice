package com.lld.connect4.repo;

import com.lld.connect4.domain.game.Game;
import com.lld.connect4.domain.game.GameId;

import java.util.Optional;

/**
 * Repository interface for game persistence.
 * Follows Repository pattern and Dependency Inversion Principle.
 */
public interface GameRepository {
    /**
     * Saves a game to the repository.
     */
    void save(Game game);

    /**
     * Finds a game by its ID.
     */
    Optional<Game> findById(GameId gameId);

    /**
     * Checks if a game exists with the given ID.
     */
    boolean exists(GameId gameId);

    /**
     * Deletes a game from the repository.
     */
    void delete(GameId gameId);
}

