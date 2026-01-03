package com.lld.connect4.repo;

import com.lld.connect4.domain.game.Game;
import com.lld.connect4.domain.game.GameId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of GameRepository.
 * Thread-safe implementation using ConcurrentHashMap.
 * Follows Repository pattern implementation.
 */
public class InMemoryGameRepository implements GameRepository {
    private final Map<GameId, Game> games = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        games.put(game.getId(), game);
    }

    @Override
    public Optional<Game> findById(GameId gameId) {
        if (gameId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public boolean exists(GameId gameId) {
        return gameId != null && games.containsKey(gameId);
    }

    @Override
    public void delete(GameId gameId) {
        if (gameId != null) {
            games.remove(gameId);
        }
    }
}

