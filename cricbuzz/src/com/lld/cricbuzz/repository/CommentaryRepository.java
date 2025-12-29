package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.commentary.Commentary;
import java.util.List;

/**
 * Repository interface for Commentary
 */
public interface CommentaryRepository {
    void save(Commentary commentary);
    List<Commentary> findByMatchId(String matchId);
    List<Commentary> findByBallEventId(String ballEventId);
    void delete(String commentaryId);
}

