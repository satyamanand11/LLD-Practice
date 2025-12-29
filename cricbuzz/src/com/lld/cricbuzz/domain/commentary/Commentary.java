package com.lld.cricbuzz.domain.commentary;

import java.time.LocalDateTime;

/**
 * Entity representing a commentary entry
 */
public class Commentary {
    private final String commentaryId;
    private final String matchId;
    private final String ballEventId; // Can be null for general commentary
    private final CommentaryType type;
    private final String text;
    private final LocalDateTime timestamp;
    private final String commentatorId;

    public Commentary(String commentaryId, String matchId, String ballEventId,
                     CommentaryType type, String text, String commentatorId) {
        if (commentaryId == null || matchId == null || type == null || text == null) {
            throw new IllegalArgumentException("Commentary ID, match ID, type, and text are required");
        }
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException("Commentary text cannot be empty");
        }
        this.commentaryId = commentaryId;
        this.matchId = matchId;
        this.ballEventId = ballEventId;
        this.type = type;
        this.text = text;
        this.commentatorId = commentatorId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getCommentaryId() {
        return commentaryId;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getBallEventId() {
        return ballEventId;
    }

    public CommentaryType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCommentatorId() {
        return commentatorId;
    }
}

