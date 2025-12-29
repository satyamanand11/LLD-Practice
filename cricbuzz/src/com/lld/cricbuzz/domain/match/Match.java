package com.lld.cricbuzz.domain.match;

import com.lld.cricbuzz.domain.tournament.MatchFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Match Aggregate Root
 * Manages match lifecycle, innings, squads, and scoring
 */
public class Match {
    private final String matchId;
    private final String tournamentId;
    private final MatchFormat format;
    private String venue;
    private String team1Id;
    private String team2Id;
    private MatchStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // Toss information
    private String tossWinnerId;
    private boolean tossWinnerChoseBatting;
    
    // Squads
    private Map<String, List<String>> teamSquads; // teamId -> list of playerIds
    
    // Innings
    private final List<Innings> innings;
    private int currentInningsNumber;
    
    // Umpires and officials
    private List<String> umpireIds;
    private List<String> scorerIds;

    public Match(String matchId, String tournamentId, MatchFormat format,
                 String team1Id, String team2Id, String venue) {
        if (matchId == null || tournamentId == null || format == null ||
            team1Id == null || team2Id == null || venue == null) {
            throw new IllegalArgumentException("All required fields must be provided");
        }
        if (team1Id.equals(team2Id)) {
            throw new IllegalArgumentException("Team 1 and Team 2 must be different");
        }
        this.matchId = matchId;
        this.tournamentId = tournamentId;
        this.format = format;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.venue = venue;
        this.status = MatchStatus.SCHEDULED;
        this.innings = new ArrayList<>();
        this.currentInningsNumber = 0;
        this.teamSquads = new HashMap<>();
        this.umpireIds = new ArrayList<>();
        this.scorerIds = new ArrayList<>();
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setSquad(String teamId, List<String> playerIds) {
        if (teamId == null || !teamId.equals(team1Id) && !teamId.equals(team2Id)) {
            throw new IllegalArgumentException("Invalid team ID");
        }
        if (playerIds == null || playerIds.isEmpty()) {
            throw new IllegalArgumentException("Squad cannot be empty");
        }
        if (status != MatchStatus.SCHEDULED && status != MatchStatus.TOSS_PENDING) {
            throw new IllegalStateException("Cannot set squad after match has started");
        }
        this.teamSquads.put(teamId, new ArrayList<>(playerIds));
    }

    public void replacePlayer(String teamId, String oldPlayerId, String newPlayerId) {
        if (status != MatchStatus.SCHEDULED && status != MatchStatus.TOSS_PENDING) {
            throw new IllegalStateException("Cannot replace players after match has started");
        }
        List<String> squad = teamSquads.get(teamId);
        if (squad == null) {
            throw new IllegalArgumentException("Team not found in match");
        }
        int index = squad.indexOf(oldPlayerId);
        if (index == -1) {
            throw new IllegalArgumentException("Player not found in squad");
        }
        squad.set(index, newPlayerId);
    }

    public void recordToss(String tossWinnerId, boolean choseBatting) {
        if (status != MatchStatus.SCHEDULED && status != MatchStatus.TOSS_PENDING) {
            throw new IllegalStateException("Toss can only be recorded before match starts");
        }
        if (!tossWinnerId.equals(team1Id) && !tossWinnerId.equals(team2Id)) {
            throw new IllegalArgumentException("Toss winner must be one of the playing teams");
        }
        this.tossWinnerId = tossWinnerId;
        this.tossWinnerChoseBatting = choseBatting;
        this.status = MatchStatus.TOSS_PENDING;
    }

    public void startMatch() {
        if (status != MatchStatus.TOSS_PENDING) {
            throw new IllegalStateException("Match can only start after toss");
        }
        if (teamSquads.size() != 2) {
            throw new IllegalStateException("Both teams must have squads set");
        }
        this.status = MatchStatus.LIVE;
        this.startTime = LocalDateTime.now();
        startNewInnings();
    }

    private void startNewInnings() {
        currentInningsNumber++;
        String battingTeamId = getBattingTeamForInnings(currentInningsNumber);
        String bowlingTeamId = battingTeamId.equals(team1Id) ? team2Id : team1Id;
        
        String inningsId = matchId + "_INN" + currentInningsNumber;
        Innings innings = new Innings(inningsId, matchId, currentInningsNumber,
                                     battingTeamId, bowlingTeamId);
        this.innings.add(innings);
    }

    private String getBattingTeamForInnings(int inningsNumber) {
        if (inningsNumber == 1) {
            return tossWinnerChoseBatting ? tossWinnerId : 
                   (tossWinnerId.equals(team1Id) ? team2Id : team1Id);
        } else if (inningsNumber == 2) {
            return tossWinnerChoseBatting ? 
                   (tossWinnerId.equals(team1Id) ? team2Id : team1Id) : tossWinnerId;
        } else {
            // For Test matches with multiple innings
            return (inningsNumber % 2 == 1) ? team1Id : team2Id;
        }
    }

    public void addInnings(Innings innings) {
        if (!innings.getMatchId().equals(this.matchId)) {
            throw new IllegalArgumentException("Innings match ID mismatch");
        }
        this.innings.add(innings);
    }

    public void completeInnings() {
        if (innings.isEmpty()) {
            throw new IllegalStateException("No innings in progress");
        }
        Innings currentInnings = innings.get(innings.size() - 1);
        if (currentInnings.isComplete()) {
            throw new IllegalStateException("Current innings is already complete");
        }
        currentInnings.complete();
        
        // Check if match should continue
        if (shouldStartNextInnings()) {
            this.status = MatchStatus.INNINGS_BREAK;
            startNewInnings();
            this.status = MatchStatus.LIVE;
        } else {
            completeMatch();
        }
    }

    private boolean shouldStartNextInnings() {
        if (format == MatchFormat.T20 || format == MatchFormat.ODI) {
            return currentInningsNumber < 2;
        } else { // TEST
            // Test match logic: 4 innings typically, but can be declared
            return currentInningsNumber < 4;
        }
    }

    public void completeMatch() {
        this.status = MatchStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void addUmpire(String umpireId) {
        if (umpireId == null || umpireId.trim().isEmpty()) {
            throw new IllegalArgumentException("Umpire ID cannot be null or empty");
        }
        if (!umpireIds.contains(umpireId)) {
            umpireIds.add(umpireId);
        }
    }

    public void addScorer(String scorerId) {
        if (scorerId == null || scorerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Scorer ID cannot be null or empty");
        }
        if (!scorerIds.contains(scorerId)) {
            scorerIds.add(scorerId);
        }
    }

    public Innings getCurrentInnings() {
        if (innings.isEmpty()) {
            return null;
        }
        return innings.get(innings.size() - 1);
    }

    // Getters
    public String getMatchId() {
        return matchId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public String getVenue() {
        return venue;
    }

    public String getTeam1Id() {
        return team1Id;
    }

    public String getTeam2Id() {
        return team2Id;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getTossWinnerId() {
        return tossWinnerId;
    }

    public boolean isTossWinnerChoseBatting() {
        return tossWinnerChoseBatting;
    }

    public Map<String, List<String>> getTeamSquads() {
        Map<String, List<String>> copy = new HashMap<>();
        teamSquads.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
        return copy;
    }

    public List<Innings> getInnings() {
        return new ArrayList<>(innings);
    }

    public int getCurrentInningsNumber() {
        return currentInningsNumber;
    }

    public List<String> getUmpireIds() {
        return new ArrayList<>(umpireIds);
    }

    public List<String> getScorerIds() {
        return new ArrayList<>(scorerIds);
    }
}

