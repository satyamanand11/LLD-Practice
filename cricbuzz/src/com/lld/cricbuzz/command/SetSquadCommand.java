package com.lld.cricbuzz.command;

import com.lld.cricbuzz.service.MatchService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to set squad for a team in a match
 */
public class SetSquadCommand implements Command {
    private final MatchService matchService;
    private final String matchId;
    private final String teamId;
    private final List<String> playerIds;
    private final String executorId;
    private final LocalDateTime timestamp;
    private boolean executed = false;
    private List<String> previousSquad;

    public SetSquadCommand(MatchService matchService, String matchId,
                          String teamId, List<String> playerIds, String executorId) {
        if (matchService == null || matchId == null || teamId == null || 
            playerIds == null || executorId == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
        this.matchService = matchService;
        this.matchId = matchId;
        this.teamId = teamId;
        this.playerIds = List.copyOf(playerIds); // Immutable copy
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            // Store previous squad for undo
            var match = matchService.getMatch(matchId);
            previousSquad = match.getTeamSquads().getOrDefault(teamId, List.of());
            
            matchService.setSquad(matchId, teamId, playerIds);
            executed = true;
            
            return CommandResult.success(
                "Squad set for team " + teamId + " with " + playerIds.size() + " players",
                playerIds
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to set squad: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed) {
            return CommandResult.failure("Command not executed yet");
        }
        
        if (previousSquad == null || previousSquad.isEmpty()) {
            // No previous squad - can't fully undo, but could clear
            return CommandResult.failure("Cannot undo: No previous squad to restore");
        }
        
        try {
            matchService.setSquad(matchId, teamId, previousSquad);
            return CommandResult.success("Squad restored to previous state");
        } catch (Exception e) {
            return CommandResult.failure("Failed to undo: " + e.getMessage());
        }
    }

    @Override
    public boolean canUndo() {
        return executed && previousSquad != null && !previousSquad.isEmpty();
    }

    @Override
    public String getDescription() {
        return "Set Squad for Team " + teamId + " (" + playerIds.size() + " players)";
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }
}

