package com.lld.cricbuzz.command;

import com.lld.cricbuzz.service.MatchService;

import java.time.LocalDateTime;

/**
 * Command to start a match
 */
public class StartMatchCommand implements Command {
    private final MatchService matchService;
    private final String matchId;
    private final String executorId;
    private final LocalDateTime timestamp;
    private boolean executed = false;

    public StartMatchCommand(MatchService matchService, String matchId, String executorId) {
        if (matchService == null || matchId == null || executorId == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
        this.matchService = matchService;
        this.matchId = matchId;
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            matchService.startMatch(matchId);
            executed = true;
            
            return CommandResult.success(
                "Match " + matchId + " started successfully",
                matchId
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to start match: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed) {
            return CommandResult.failure("Command not executed yet");
        }
        
        // Starting a match is typically irreversible
        // In a real system, you might pause/stop the match instead
        return CommandResult.failure("Cannot undo match start - match is now live");
    }

    @Override
    public boolean canUndo() {
        return false; // Match start cannot be undone
    }

    @Override
    public String getDescription() {
        return "Start Match " + matchId;
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

