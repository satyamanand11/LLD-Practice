package com.lld.cricbuzz.command;

import com.lld.cricbuzz.service.MatchService;

import java.time.LocalDateTime;

/**
 * Command to assign an umpire to a match
 */
public class AssignUmpireCommand implements Command {
    private final MatchService matchService;
    private final String matchId;
    private final String umpireId;
    private final String executorId;
    private final LocalDateTime timestamp;
    private boolean executed = false;
    private boolean wasAlreadyAssigned = false;

    public AssignUmpireCommand(MatchService matchService, String matchId, 
                               String umpireId, String executorId) {
        if (matchService == null || matchId == null || umpireId == null || executorId == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
        this.matchService = matchService;
        this.matchId = matchId;
        this.umpireId = umpireId;
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            // Check if umpire already assigned (for undo tracking)
            var match = matchService.getMatch(matchId);
            wasAlreadyAssigned = match.getUmpireIds().contains(umpireId);
            
            matchService.addUmpire(matchId, umpireId);
            executed = true;
            
            return CommandResult.success(
                "Umpire " + umpireId + " assigned to match " + matchId,
                umpireId
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to assign umpire: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed) {
            return CommandResult.failure("Command not executed yet");
        }
        
        if (wasAlreadyAssigned) {
            // Can't undo if umpire was already assigned before
            return CommandResult.failure("Cannot undo: Umpire was already assigned");
        }
        
        try {
            // Note: MatchService doesn't have removeUmpire, so we'd need to add that
            // For now, return a message indicating undo is not fully supported
            return CommandResult.failure("Umpire removal not yet implemented");
        } catch (Exception e) {
            return CommandResult.failure("Failed to undo: " + e.getMessage());
        }
    }

    @Override
    public boolean canUndo() {
        return executed && !wasAlreadyAssigned;
    }

    @Override
    public String getDescription() {
        return "Assign Umpire " + umpireId + " to Match " + matchId;
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

