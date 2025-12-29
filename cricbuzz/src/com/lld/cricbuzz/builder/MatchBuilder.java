package com.lld.cricbuzz.builder;

import com.lld.cricbuzz.domain.match.Match;
import com.lld.cricbuzz.domain.tournament.MatchFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builder Pattern for Match construction
 * Allows step-by-step construction of Match with validation
 * 
 * Example usage:
 * Match match = new MatchBuilder()
 *     .setTournament(tournamentId)
 *     .setFormat(MatchFormat.ODI)
 *     .setTeams(team1Id, team2Id)
 *     .setVenue("Wankhede Stadium")
 *     .setScheduledTime(LocalDateTime.now())
 *     .addUmpire("UMP_001")
 *     .addScorer("SCR_001")
 *     .setSquad(team1Id, team1Squad)
 *     .setSquad(team2Id, team2Squad)
 *     .build();
 */
public class MatchBuilder {
    private String matchId;
    private String tournamentId;
    private MatchFormat format;
    private String team1Id;
    private String team2Id;
    private String venue;
    private LocalDateTime scheduledTime;
    private final List<String> umpireIds;
    private final List<String> scorerIds;
    private final Map<String, List<String>> teamSquads;

    public MatchBuilder() {
        this.umpireIds = new ArrayList<>();
        this.scorerIds = new ArrayList<>();
        this.teamSquads = new HashMap<>();
    }

    /**
     * Set match ID (auto-generated if not provided)
     */
    public MatchBuilder setMatchId(String matchId) {
        if (matchId == null || matchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Match ID cannot be null or empty");
        }
        this.matchId = matchId;
        return this;
    }

    /**
     * Set tournament for this match (required)
     */
    public MatchBuilder setTournament(String tournamentId) {
        if (tournamentId == null || tournamentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament ID cannot be null or empty");
        }
        this.tournamentId = tournamentId;
        return this;
    }

    /**
     * Set match format (required)
     */
    public MatchBuilder setFormat(MatchFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Match format cannot be null");
        }
        this.format = format;
        return this;
    }

    /**
     * Set both teams (required)
     */
    public MatchBuilder setTeams(String team1Id, String team2Id) {
        if (team1Id == null || team2Id == null) {
            throw new IllegalArgumentException("Both team IDs are required");
        }
        if (team1Id.equals(team2Id)) {
            throw new IllegalArgumentException("Team 1 and Team 2 must be different");
        }
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        return this;
    }

    /**
     * Set venue (required)
     */
    public MatchBuilder setVenue(String venue) {
        if (venue == null || venue.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue cannot be null or empty");
        }
        this.venue = venue;
        return this;
    }

    /**
     * Set scheduled time (optional)
     */
    public MatchBuilder setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
        return this;
    }

    /**
     * Add an umpire (optional, can be called multiple times)
     */
    public MatchBuilder addUmpire(String umpireId) {
        if (umpireId == null || umpireId.trim().isEmpty()) {
            throw new IllegalArgumentException("Umpire ID cannot be null or empty");
        }
        if (!umpireIds.contains(umpireId)) {
            umpireIds.add(umpireId);
        }
        return this;
    }

    /**
     * Add multiple umpires
     */
    public MatchBuilder addUmpires(List<String> umpireIds) {
        if (umpireIds != null) {
            umpireIds.forEach(this::addUmpire);
        }
        return this;
    }

    /**
     * Add a scorer (optional, can be called multiple times)
     */
    public MatchBuilder addScorer(String scorerId) {
        if (scorerId == null || scorerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Scorer ID cannot be null or empty");
        }
        if (!scorerIds.contains(scorerId)) {
            scorerIds.add(scorerId);
        }
        return this;
    }

    /**
     * Add multiple scorers
     */
    public MatchBuilder addScorers(List<String> scorerIds) {
        if (scorerIds != null) {
            scorerIds.forEach(this::addScorer);
        }
        return this;
    }

    /**
     * Set squad for a team (optional, can be called for each team)
     */
    public MatchBuilder setSquad(String teamId, List<String> playerIds) {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty");
        }
        if (playerIds == null || playerIds.isEmpty()) {
            throw new IllegalArgumentException("Squad cannot be empty");
        }
        if (team1Id != null && !teamId.equals(team1Id) && team2Id != null && !teamId.equals(team2Id)) {
            throw new IllegalArgumentException("Team ID must match one of the playing teams");
        }
        this.teamSquads.put(teamId, new ArrayList<>(playerIds));
        return this;
    }

    /**
     * Build the Match object
     * Validates all required fields before construction
     */
    public Match build() {
        validate();
        
        // Auto-generate match ID if not provided
        if (matchId == null) {
            matchId = "MATCH_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        Match match = new Match(matchId, tournamentId, format, team1Id, team2Id, venue);
        
        // Set optional fields
        if (scheduledTime != null) {
            match.setScheduledTime(scheduledTime);
        }
        
        // Add umpires and scorers
        for (String umpireId : umpireIds) {
            match.addUmpire(umpireId);
        }
        
        for (String scorerId : scorerIds) {
            match.addScorer(scorerId);
        }
        
        // Set squads
        for (Map.Entry<String, List<String>> entry : teamSquads.entrySet()) {
            match.setSquad(entry.getKey(), entry.getValue());
        }
        
        return match;
    }

    /**
     * Validate that all required fields are set
     */
    private void validate() {
        if (tournamentId == null || tournamentId.trim().isEmpty()) {
            throw new IllegalStateException("Tournament ID is required");
        }
        if (format == null) {
            throw new IllegalStateException("Match format is required");
        }
        if (team1Id == null || team2Id == null) {
            throw new IllegalStateException("Both teams are required");
        }
        if (team1Id.equals(team2Id)) {
            throw new IllegalStateException("Team 1 and Team 2 must be different");
        }
        if (venue == null || venue.trim().isEmpty()) {
            throw new IllegalStateException("Venue is required");
        }
    }

    // Getters for accessing builder state
    public String getMatchId() {
        return matchId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public String getTeam1Id() {
        return team1Id;
    }

    public String getTeam2Id() {
        return team2Id;
    }

    public String getVenue() {
        return venue;
    }
}

