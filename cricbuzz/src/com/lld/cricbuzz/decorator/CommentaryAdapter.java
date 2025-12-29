package com.lld.cricbuzz.decorator;

import com.lld.cricbuzz.domain.commentary.Commentary;
import com.lld.cricbuzz.domain.commentary.CommentaryType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Adapter to make Commentary compatible with CommentaryComponent interface
 * Allows existing Commentary objects to be used with decorators
 */
public class CommentaryAdapter implements CommentaryComponent {
    private final Commentary commentary;

    public CommentaryAdapter(Commentary commentary) {
        if (commentary == null) {
            throw new IllegalArgumentException("Commentary cannot be null");
        }
        this.commentary = commentary;
    }

    @Override
    public String getCommentaryId() {
        return commentary.getCommentaryId();
    }

    @Override
    public String getMatchId() {
        return commentary.getMatchId();
    }

    @Override
    public String getBallEventId() {
        return commentary.getBallEventId();
    }

    @Override
    public CommentaryType getType() {
        return commentary.getType();
    }

    @Override
    public String getText() {
        return commentary.getText();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return commentary.getTimestamp();
    }

    @Override
    public String getCommentatorId() {
        return commentary.getCommentatorId();
    }

    @Override
    public String getEnhancedText() {
        return commentary.getText(); // Base text without enhancements
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.emptyMap(); // No metadata by default
    }
}

