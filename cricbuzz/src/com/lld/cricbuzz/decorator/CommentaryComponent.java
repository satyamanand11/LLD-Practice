package com.lld.cricbuzz.decorator;

import com.lld.cricbuzz.domain.commentary.CommentaryType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Component interface for Commentary Decorator Pattern
 * Defines the contract for commentary objects that can be decorated
 */
public interface CommentaryComponent {
    String getCommentaryId();
    String getMatchId();
    String getBallEventId();
    CommentaryType getType();
    String getText();
    LocalDateTime getTimestamp();
    String getCommentatorId();
    
    /**
     * Get enhanced text with all decorations applied
     */
    String getEnhancedText();
    
    /**
     * Get metadata added by decorators (statistics, translations, highlights)
     */
    Map<String, Object> getMetadata();
}

