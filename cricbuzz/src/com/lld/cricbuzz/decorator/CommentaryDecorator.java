package com.lld.cricbuzz.decorator;

import com.lld.cricbuzz.domain.commentary.CommentaryType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Decorator for Commentary
 * Base class for all commentary decorators
 * Follows Decorator Pattern to add features dynamically
 */
public abstract class CommentaryDecorator implements CommentaryComponent {
    protected final CommentaryComponent wrapped;

    public CommentaryDecorator(CommentaryComponent wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Wrapped commentary cannot be null");
        }
        this.wrapped = wrapped;
    }

    // Delegate all base methods to wrapped component
    @Override
    public String getCommentaryId() {
        return wrapped.getCommentaryId();
    }

    @Override
    public String getMatchId() {
        return wrapped.getMatchId();
    }

    @Override
    public String getBallEventId() {
        return wrapped.getBallEventId();
    }

    @Override
    public CommentaryType getType() {
        return wrapped.getType();
    }

    @Override
    public String getText() {
        return wrapped.getText();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return wrapped.getTimestamp();
    }

    @Override
    public String getCommentatorId() {
        return wrapped.getCommentatorId();
    }

    @Override
    public String getEnhancedText() {
        return wrapped.getEnhancedText();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return new HashMap<>(wrapped.getMetadata());
    }
}

