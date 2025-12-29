package com.lld.cricbuzz.domain.tournament;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tournament Aggregate Root
 * Manages tournament information, participating teams, and matches
 */
public class Tournament {
    private final String tournamentId;
    private String name;
    private MatchFormat format;
    private TournamentStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> teamIds;
    private List<String> matchIds;

    public Tournament(String tournamentId, String name, MatchFormat format) {
        if (tournamentId == null || tournamentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament name cannot be null or empty");
        }
        this.tournamentId = tournamentId;
        this.name = name;
        this.format = Objects.requireNonNull(format, "Match format cannot be null");
        this.status = TournamentStatus.DRAFT;
        this.teamIds = new ArrayList<>();
        this.matchIds = new ArrayList<>();
    }

    public void addTeam(String teamId) {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty");
        }
        if (teamIds.contains(teamId)) {
            throw new IllegalArgumentException("Team already in tournament");
        }
        teamIds.add(teamId);
    }

    public void removeTeam(String teamId) {
        if (!teamIds.remove(teamId)) {
            throw new IllegalArgumentException("Team not found in tournament");
        }
    }

    public void addMatch(String matchId) {
        if (matchId == null || matchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Match ID cannot be null or empty");
        }
        if (matchIds.contains(matchId)) {
            throw new IllegalArgumentException("Match already in tournament");
        }
        matchIds.add(matchId);
    }

    public void updateStatus(TournamentStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public void setDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public String getTournamentId() {
        return tournamentId;
    }

    public String getName() {
        return name;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<String> getTeamIds() {
        return new ArrayList<>(teamIds);
    }

    public List<String> getMatchIds() {
        return new ArrayList<>(matchIds);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament name cannot be null or empty");
        }
        this.name = name;
    }
}

