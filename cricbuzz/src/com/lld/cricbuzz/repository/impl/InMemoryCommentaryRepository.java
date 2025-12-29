package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.commentary.Commentary;
import com.lld.cricbuzz.repository.CommentaryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryCommentaryRepository implements CommentaryRepository {
    private final Map<String, Commentary> commentaries = new ConcurrentHashMap<>();
    private final Map<String, List<String>> matchCommentaries = new ConcurrentHashMap<>();
    private final Map<String, List<String>> ballEventCommentaries = new ConcurrentHashMap<>();

    @Override
    public void save(Commentary commentary) {
        commentaries.put(commentary.getCommentaryId(), commentary);
        matchCommentaries.computeIfAbsent(commentary.getMatchId(), k -> new ArrayList<>())
            .add(commentary.getCommentaryId());
        if (commentary.getBallEventId() != null) {
            ballEventCommentaries.computeIfAbsent(commentary.getBallEventId(), k -> new ArrayList<>())
                .add(commentary.getCommentaryId());
        }
    }

    @Override
    public List<Commentary> findByMatchId(String matchId) {
        List<String> commentaryIds = matchCommentaries.getOrDefault(matchId, new ArrayList<>());
        return commentaryIds.stream()
            .map(commentaries::get)
            .filter(c -> c != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<Commentary> findByBallEventId(String ballEventId) {
        List<String> commentaryIds = ballEventCommentaries.getOrDefault(ballEventId, new ArrayList<>());
        return commentaryIds.stream()
            .map(commentaries::get)
            .filter(c -> c != null)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String commentaryId) {
        Commentary commentary = commentaries.remove(commentaryId);
        if (commentary != null) {
            matchCommentaries.getOrDefault(commentary.getMatchId(), new ArrayList<>())
                .remove(commentaryId);
            if (commentary.getBallEventId() != null) {
                ballEventCommentaries.getOrDefault(commentary.getBallEventId(), new ArrayList<>())
                    .remove(commentaryId);
            }
        }
    }
}

