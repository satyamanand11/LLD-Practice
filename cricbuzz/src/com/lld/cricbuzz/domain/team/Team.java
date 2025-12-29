package com.lld.cricbuzz.domain.team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Team Aggregate Root
 * Manages team information and squad composition
 */
public class Team {
    private final String teamId;
    private String name;
    private String country;
    private TeamStatus status;
    private List<String> playerIds; // Squad members

    public Team(String teamId, String name, String country) {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        this.teamId = teamId;
        this.name = name;
        this.country = country;
        this.status = TeamStatus.ACTIVE;
        this.playerIds = new ArrayList<>();
    }

    public void addPlayer(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Player ID cannot be null or empty");
        }
        if (playerIds.contains(playerId)) {
            throw new IllegalArgumentException("Player already in team");
        }
        playerIds.add(playerId);
    }

    public void removePlayer(String playerId) {
        if (!playerIds.remove(playerId)) {
            throw new IllegalArgumentException("Player not found in team");
        }
    }

    public void updateStatus(TeamStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    // Getters
    public String getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public List<String> getPlayerIds() {
        return new ArrayList<>(playerIds);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

