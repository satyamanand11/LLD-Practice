package com.lld.connect4.domain.game;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique game identifier.
 * Follows DDD principles by making IDs immutable value objects.
 */
public class GameId {
    private final String id;

    private GameId(String id) {
        this.id = id;
    }

    public static GameId generate() {
        return new GameId(UUID.randomUUID().toString());
    }

    public static GameId from(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Game ID cannot be null or empty");
        }
        return new GameId(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameId gameId = (GameId) o;
        return Objects.equals(id, gameId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}

