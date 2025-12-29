package com.lld.cricbuzz.command;

import com.lld.cricbuzz.service.MatchService;

import java.time.LocalDateTime;

/**
 * Command to record toss result
 */
public class RecordTossCommand implements Command {
    private final MatchService matchService;
    private final String matchId;
    private final String tossWinnerId;
    private final boolean choseBatting;
    private final String executorId;
    private final LocalDateTime timestamp;
    private boolean executed = false;
    private String previousTossWinnerId;
    private boolean previousChoseBatting;

    public RecordTossCommand(MatchService matchService, String matchId,
                            String tossWinnerId, boolean choseBatting, String executorId) {
        if (matchService == null || matchId == null || tossWinnerId == null || executorId == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
        this.matchService = matchService;
        this.matchId = matchId;
        this.tossWinnerId = tossWinnerId;
        this.choseBatting = choseBatting;
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            // Store previous state for undo
            var match = matchService.getMatch(matchId);
            previousTossWinnerId = match.getTossWinnerId();
            previousChoseBatting = match.isTossWinnerChoseBatting();
            
            matchService.recordToss(matchId, tossWinnerId, choseBatting);
            executed = true;
            
            String choice = choseBatting ? "batting" : "bowling";
            return CommandResult.success(
                "Toss recorded: " + tossWinnerId + " won and chose " + choice,
                tossWinnerId
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to record toss: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed) {
            return CommandResult.failure("Command not executed yet");
        }
        
        if (previousTossWinnerId == null) {
            // No previous toss recorded
            return CommandResult.failure("Cannot undo: No previous toss to restore");
        }
        
        try {
            matchService.recordToss(matchId, previousTossWinnerId, previousChoseBatting);
            return CommandResult.success("Toss record undone");
        } catch (Exception e) {
            return CommandResult.failure("Failed to undo: " + e.getMessage());
        }
    }

    @Override
    public boolean canUndo() {
        return executed && previousTossWinnerId != null;
    }

    @Override
    public String getDescription() {
        String choice = choseBatting ? "batting" : "bowling";
        return "Record Toss: " + tossWinnerId + " chose " + choice;
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

