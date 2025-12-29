package com.lld.cricbuzz.builder;

import com.lld.cricbuzz.domain.tournament.MatchFormat;
import com.lld.cricbuzz.domain.tournament.Tournament;
import com.lld.cricbuzz.domain.tournament.TournamentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder Pattern for Tournament construction
 * Allows step-by-step construction of Tournament with validation
 * 
 * Example usage:
 * Tournament tournament = new TournamentBuilder()
 *     .setName("ICC World Cup 2024")
 *     .setFormat(MatchFormat.ODI)
 *     .setDates(LocalDate.now(), LocalDate.now().plusDays(30))
 *     .addTeam(team1Id)
 *     .addTeam(team2Id)
 *     .addTeam(team3Id)
 *     .build();
 */
public class TournamentBuilder {
    private String tournamentId;
    private String name;
    private MatchFormat format;
    private LocalDate startDate;
    private LocalDate endDate;
    private TournamentStatus initialStatus;
    private final List<String> teamIds;
    private final List<String> matchIds;

    public TournamentBuilder() {
        this.teamIds = new ArrayList<>();
        this.matchIds = new ArrayList<>();
        this.initialStatus = TournamentStatus.DRAFT;
    }

    /**
     * Set tournament ID (auto-generated if not provided)
     */
    public TournamentBuilder setTournamentId(String tournamentId) {
        if (tournamentId == null || tournamentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament ID cannot be null or empty");
        }
        this.tournamentId = tournamentId;
        return this;
    }

    /**
     * Set tournament name (required)
     */
    public TournamentBuilder setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament name cannot be null or empty");
        }
        this.name = name;
        return this;
    }

    /**
     * Set match format (required)
     */
    public TournamentBuilder setFormat(MatchFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Match format cannot be null");
        }
        this.format = format;
        return this;
    }

    /**
     * Set tournament dates (optional)
     */
    public TournamentBuilder setDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    /**
     * Set initial status (optional, defaults to DRAFT)
     */
    public TournamentBuilder setInitialStatus(TournamentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.initialStatus = status;
        return this;
    }

    /**
     * Add a team to the tournament (optional, can be called multiple times)
     */
    public TournamentBuilder addTeam(String teamId) {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty");
        }
        if (teamIds.contains(teamId)) {
            throw new IllegalArgumentException("Team already added to tournament");
        }
        teamIds.add(teamId);
        return this;
    }

    /**
     * Add multiple teams
     */
    public TournamentBuilder addTeams(List<String> teamIds) {
        if (teamIds != null) {
            teamIds.forEach(this::addTeam);
        }
        return this;
    }

    /**
     * Add a match to the tournament (optional, can be called multiple times)
     */
    public TournamentBuilder addMatch(String matchId) {
        if (matchId == null || matchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Match ID cannot be null or empty");
        }
        if (matchIds.contains(matchId)) {
            throw new IllegalArgumentException("Match already added to tournament");
        }
        matchIds.add(matchId);
        return this;
    }

    /**
     * Add multiple matches
     */
    public TournamentBuilder addMatches(List<String> matchIds) {
        if (matchIds != null) {
            matchIds.forEach(this::addMatch);
        }
        return this;
    }

    /**
     * Build the Tournament object
     * Validates all required fields before construction
     */
    public Tournament build() {
        validate();
        
        // Auto-generate tournament ID if not provided
        if (tournamentId == null) {
            tournamentId = "TOUR_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        Tournament tournament = new Tournament(tournamentId, name, format);
        
        // Set optional fields
        if (startDate != null || endDate != null) {
            tournament.setDates(startDate, endDate);
        }
        
        tournament.updateStatus(initialStatus);
        
        // Add teams
        for (String teamId : teamIds) {
            tournament.addTeam(teamId);
        }
        
        // Add matches
        for (String matchId : matchIds) {
            tournament.addMatch(matchId);
        }
        
        return tournament;
    }

    /**
     * Validate that all required fields are set
     */
    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Tournament name is required");
        }
        if (format == null) {
            throw new IllegalStateException("Match format is required");
        }
    }

    // Getters for accessing builder state
    public String getTournamentId() {
        return tournamentId;
    }

    public String getName() {
        return name;
    }

    public MatchFormat getFormat() {
        return format;
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
}

