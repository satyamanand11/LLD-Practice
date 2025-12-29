package com.lld.cricbuzz.domain.player;

import java.util.Objects;

/**
 * Player Aggregate Root
 * Manages player information and role
 */
public class Player {
    private final String playerId;
    private String name;
    private int age;
    private String country;
    private PlayerRole role;
    private boolean isActive;

    public Player(String playerId, String name, int age, String country, PlayerRole role) {
        if (playerId == null || playerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Player ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        this.playerId = playerId;
        this.name = name;
        this.age = age;
        this.country = Objects.requireNonNull(country, "Country cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.isActive = true;
    }

    public void updateRole(PlayerRole role) {
        this.role = Objects.requireNonNull(role, "Role cannot be null");
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getCountry() {
        return country;
    }

    public PlayerRole getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.name = name;
    }

    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        this.age = age;
    }
}

