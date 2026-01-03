package com.lld.connect4.domain.game;

import java.util.Objects;

/**
 * Domain entity representing a player in the Connect4 game.
 * Immutable value object following DDD principles.
 */
public class Player {
    private final PlayerId id;
    private final String name;
    private final Disc disc;

    public Player(PlayerId id, String name, Disc disc) {
        if (id == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        if (disc == null) {
            throw new IllegalArgumentException("Disc cannot be null");
        }
        this.id = id;
        this.name = name;
        this.disc = disc;
    }

    public PlayerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Disc getDisc() {
        return disc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", disc=" + disc +
                '}';
    }
}

