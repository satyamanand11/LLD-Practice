package com.lld.cricbuzz.command;

import com.lld.cricbuzz.domain.commentary.Commentary;
import com.lld.cricbuzz.domain.commentary.CommentaryType;
import com.lld.cricbuzz.service.CommentaryService;

import java.time.LocalDateTime;

/**
 * Command to add commentary to a match
 */
public class AddCommentaryCommand implements Command {
    private final CommentaryService commentaryService;
    private final String matchId;
    private final String ballEventId;
    private final CommentaryType type;
    private final String text;
    private final String commentatorId;
    private final String executorId;
    private final LocalDateTime timestamp;
    private Commentary createdCommentary;
    private boolean executed = false;

    public AddCommentaryCommand(CommentaryService commentaryService, String matchId,
                                String ballEventId, CommentaryType type, String text,
                                String commentatorId, String executorId) {
        if (commentaryService == null || matchId == null || type == null || 
            text == null || commentatorId == null || executorId == null) {
            throw new IllegalArgumentException("All required parameters must be provided");
        }
        this.commentaryService = commentaryService;
        this.matchId = matchId;
        this.ballEventId = ballEventId;
        this.type = type;
        this.text = text;
        this.commentatorId = commentatorId;
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            createdCommentary = commentaryService.addCommentary(
                matchId, ballEventId, type, text, commentatorId
            );
            executed = true;
            
            return CommandResult.success(
                "Commentary added successfully",
                createdCommentary
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to add commentary: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed || createdCommentary == null) {
            return CommandResult.failure("Command not executed yet");
        }
        
        // Note: Would need deleteCommentary in CommentaryService
        return CommandResult.failure("Commentary deletion not yet implemented");
    }

    @Override
    public boolean canUndo() {
        return executed && createdCommentary != null;
    }

    @Override
    public String getDescription() {
        return "Add " + type + " commentary to Match " + matchId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    public Commentary getCreatedCommentary() {
        return createdCommentary;
    }
}

