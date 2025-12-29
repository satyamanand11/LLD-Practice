package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.commentary.Commentary;
import com.lld.cricbuzz.domain.commentary.CommentaryType;
import com.lld.cricbuzz.repository.CommentaryRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing commentary
 */
public class CommentaryService {
    private final CommentaryRepository commentaryRepository;

    public CommentaryService(CommentaryRepository commentaryRepository) {
        this.commentaryRepository = commentaryRepository;
    }

    public Commentary addCommentary(String matchId, String ballEventId,
                                   CommentaryType type, String text, String commentatorId) {
        String commentaryId = "COMM_" + UUID.randomUUID().toString().substring(0, 8);
        Commentary commentary = new Commentary(commentaryId, matchId, ballEventId, type, text, commentatorId);
        commentaryRepository.save(commentary);
        return commentary;
    }

    public List<Commentary> getCommentaryByMatch(String matchId) {
        return commentaryRepository.findByMatchId(matchId);
    }

    public List<Commentary> getCommentaryByBallEvent(String ballEventId) {
        return commentaryRepository.findByBallEventId(ballEventId);
    }
}

