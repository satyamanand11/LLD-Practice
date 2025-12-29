package com.lld.cricbuzz.service;

import com.lld.cricbuzz.decorator.*;
import com.lld.cricbuzz.domain.commentary.Commentary;
import com.lld.cricbuzz.domain.commentary.CommentaryType;
import com.lld.cricbuzz.repository.CommentaryRepository;
import com.lld.cricbuzz.repository.MatchRepository;
import com.lld.cricbuzz.repository.PlayerMatchStatsRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enhanced Commentary Service using Decorator Pattern
 * Allows dynamic enhancement of commentary with statistics, translations, and highlights
 */
public class EnhancedCommentaryService {
    private final CommentaryRepository commentaryRepository;
    private final MatchRepository matchRepository;
    private final PlayerMatchStatsRepository statsRepository;

    public EnhancedCommentaryService(CommentaryRepository commentaryRepository,
                                    MatchRepository matchRepository,
                                    PlayerMatchStatsRepository statsRepository) {
        this.commentaryRepository = commentaryRepository;
        this.matchRepository = matchRepository;
        this.statsRepository = statsRepository;
    }

    /**
     * Add commentary with optional enhancements
     */
    public CommentaryComponent addCommentary(String matchId, String ballEventId,
                                           CommentaryType type, String text,
                                           String commentatorId,
                                           boolean includeStats,
                                           boolean includeTranslation,
                                           String targetLanguage,
                                           HighlightsDecorator.HighlightType highlightType) {
        String commentaryId = "COMM_" + UUID.randomUUID().toString().substring(0, 8);
        Commentary commentary = new Commentary(commentaryId, matchId, ballEventId, type, text, commentatorId);
        commentaryRepository.save(commentary);

        // Start with base commentary
        CommentaryComponent enhanced = new CommentaryAdapter(commentary);

        // Apply decorators based on requirements
        if (includeStats) {
            enhanced = new StatisticsDecorator(enhanced, matchRepository, statsRepository);
        }

        if (includeTranslation && targetLanguage != null) {
            enhanced = new TranslationDecorator(enhanced, targetLanguage);
        }

        if (highlightType != null) {
            enhanced = new HighlightsDecorator(enhanced, highlightType);
        }

        return enhanced;
    }

    /**
     * Get enhanced commentary for a match
     */
    public List<CommentaryComponent> getEnhancedCommentary(String matchId,
                                                          boolean includeStats,
                                                          boolean includeTranslation,
                                                          String targetLanguage) {
        List<Commentary> commentaries = commentaryRepository.findByMatchId(matchId);
        
        return commentaries.stream()
            .map(commentary -> {
                CommentaryComponent enhanced = new CommentaryAdapter(commentary);
                
                if (includeStats) {
                    enhanced = new StatisticsDecorator(enhanced, matchRepository, statsRepository);
                }
                
                if (includeTranslation && targetLanguage != null) {
                    enhanced = new TranslationDecorator(enhanced, targetLanguage);
                }
                
                // Auto-detect highlight type based on commentary type
                HighlightsDecorator.HighlightType highlightType = detectHighlightType(commentary.getType());
                if (highlightType != null) {
                    enhanced = new HighlightsDecorator(enhanced, highlightType);
                }
                
                return enhanced;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get enhanced commentary with player-specific statistics
     */
    public CommentaryComponent addCommentaryWithPlayerStats(String matchId, String ballEventId,
                                                           CommentaryType type, String text,
                                                           String commentatorId, String playerId) {
        String commentaryId = "COMM_" + UUID.randomUUID().toString().substring(0, 8);
        Commentary commentary = new Commentary(commentaryId, matchId, ballEventId, type, text, commentatorId);
        commentaryRepository.save(commentary);

        CommentaryComponent enhanced = new CommentaryAdapter(commentary);
        enhanced = new StatisticsDecorator(enhanced, matchRepository, statsRepository, playerId);
        
        // Add highlights based on type
        HighlightsDecorator.HighlightType highlightType = detectHighlightType(type);
        if (highlightType != null) {
            enhanced = new HighlightsDecorator(enhanced, highlightType);
        }

        return enhanced;
    }

    private HighlightsDecorator.HighlightType detectHighlightType(CommentaryType type) {
        return switch (type) {
            case WICKET -> HighlightsDecorator.HighlightType.WICKET;
            case BOUNDARY, SIX -> HighlightsDecorator.HighlightType.BOUNDARY;
            case MILESTONE -> HighlightsDecorator.HighlightType.MILESTONE;
            default -> HighlightsDecorator.HighlightType.EXCITING;
        };
    }
}

