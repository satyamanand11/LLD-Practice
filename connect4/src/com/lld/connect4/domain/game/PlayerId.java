package com.lld.connect4.domain.game;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique player identifier.
 */
public class PlayerId {
    private final String id;

    private PlayerId(String id) {
        this.id = id;
    }

    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID().toString());
    }

    public static PlayerId from(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Player ID cannot be null or empty");
        }
        return new PlayerId(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerId playerId = (PlayerId) o;
        return Objects.equals(id, playerId.id);
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

