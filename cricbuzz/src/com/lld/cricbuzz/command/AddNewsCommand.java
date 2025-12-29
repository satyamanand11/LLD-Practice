package com.lld.cricbuzz.command;

import java.time.LocalDateTime;

/**
 * Command to add news/article about a match, tournament, or player
 * Represents user-triggered content creation
 */
public class AddNewsCommand implements Command {
    private final String newsId;
    private final String title;
    private final String content;
    private final String category; // match, tournament, player, general
    private final String entityId; // matchId, tournamentId, playerId, etc.
    private final String authorId;
    private final String executorId;
    private final LocalDateTime timestamp;
    private boolean executed = false;

    // Getters for fields (used in real implementation)
    public String getContent() { return content; }
    public String getEntityId() { return entityId; }
    public String getAuthorId() { return authorId; }

    // In a real system, this would use a NewsService
    // For now, we'll simulate it

    public AddNewsCommand(String title, String content, String category,
                         String entityId, String authorId, String executorId) {
        if (title == null || content == null || category == null || 
            authorId == null || executorId == null) {
            throw new IllegalArgumentException("All required parameters must be provided");
        }
        this.newsId = "NEWS_" + System.currentTimeMillis();
        this.title = title;
        this.content = content;
        this.category = category;
        this.entityId = entityId;
        this.authorId = authorId;
        this.executorId = executorId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public CommandResult execute() {
        try {
            // In a real system, this would save to NewsRepository
            // For now, we'll just simulate success
            executed = true;
            
            return CommandResult.success(
                "News article '" + title + "' added successfully",
                newsId
            );
        } catch (Exception e) {
            return CommandResult.failure("Failed to add news: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (!executed) {
            return CommandResult.failure("Command not executed yet");
        }
        
        // In a real system, this would delete from NewsRepository
        return CommandResult.success("News article removed");
    }

    @Override
    public boolean canUndo() {
        return executed;
    }

    @Override
    public String getDescription() {
        return "Add News: " + title + " (" + category + ")";
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    public String getNewsId() {
        return newsId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }
}

