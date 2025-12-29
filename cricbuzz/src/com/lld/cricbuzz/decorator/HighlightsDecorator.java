package com.lld.cricbuzz.decorator;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorator that adds highlights to commentary
 * Enhances commentary with visual highlights and emphasis
 */
public class HighlightsDecorator extends CommentaryDecorator {
    private final HighlightType highlightType;

    public enum HighlightType {
        BOUNDARY,      // Highlight for 4s and 6s
        WICKET,        // Highlight for wickets
        MILESTONE,     // Highlight for player milestones
        EXCITING       // General exciting moment
    }

    public HighlightsDecorator(CommentaryComponent wrapped, HighlightType highlightType) {
        super(wrapped);
        if (highlightType == null) {
            throw new IllegalArgumentException("Highlight type cannot be null");
        }
        this.highlightType = highlightType;
    }

    @Override
    public String getEnhancedText() {
        String baseText = wrapped.getEnhancedText();
        return wrapWithHighlights(baseText);
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>(wrapped.getMetadata());
        metadata.put("highlight", Map.of(
            "type", highlightType.name(),
            "priority", getPriority(),
            "color", getHighlightColor()
        ));
        return metadata;
    }

    private String wrapWithHighlights(String text) {
        return switch (highlightType) {
            case BOUNDARY -> "🔥 " + text.toUpperCase() + " 🔥";
            case WICKET -> "⚡ " + text + " ⚡";
            case MILESTONE -> "⭐ " + text + " ⭐";
            case EXCITING -> "✨ " + text + " ✨";
            default -> text;
        };
    }

    private int getPriority() {
        return switch (highlightType) {
            case WICKET, MILESTONE -> 3; // High priority
            case BOUNDARY -> 2;          // Medium priority
            case EXCITING -> 1;          // Low priority
        };
    }

    private String getHighlightColor() {
        return switch (highlightType) {
            case WICKET -> "red";
            case MILESTONE -> "gold";
            case BOUNDARY -> "blue";
            case EXCITING -> "green";
        };
    }

    public HighlightType getHighlightType() {
        return highlightType;
    }
}

