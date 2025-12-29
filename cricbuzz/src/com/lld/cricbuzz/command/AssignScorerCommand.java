package com.lld.cricbuzz.command;

import com.lld.cricbuzz.service.MatchService;

import java.time.LocalDateTime;

/**
 * Command to assign a scorer to a match
 */
public class AssignScorerCommand implements Command {
    private final MatchService matchService;
    private final String matchId;
    private final String scorerId;
    private final String executorId;
    private final LocalDateTime timestamp;
    private boolean executed = false;
    private boolean wasAlreadyAssigned = false;

    public AssignScorerCommand(MatchService matchService, String matchId,
                              String scorerId, String executorId) {
        if (matchService == null || matchId == null || scorerId == null || executorId == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
        this.matchService = matchService;
        this.matchId = matchId;
        this.scorerId = scorerId;
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            var match = matchService.getMatch(matchId);
            wasAlreadyAssigned = match.getScorerIds().contains(scorerId);
            
            matchService.addScorer(matchId, scorerId);
            executed = true;
            
            return CommandResult.success(
                "Scorer " + scorerId + " assigned to match " + matchId,
                scorerId
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to assign scorer: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed) {
            return CommandResult.failure("Command not executed yet");
        }
        
        if (wasAlreadyAssigned) {
            return CommandResult.failure("Cannot undo: Scorer was already assigned");
        }
        
        return CommandResult.failure("Scorer removal not yet implemented");
    }

    @Override
    public boolean canUndo() {
        return executed && !wasAlreadyAssigned;
    }

    @Override
    public String getDescription() {
        return "Assign Scorer " + scorerId + " to Match " + matchId;
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

