package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.match.Match;
import com.lld.cricbuzz.domain.tournament.MatchFormat;
import com.lld.cricbuzz.events.EventBus;
import com.lld.cricbuzz.events.MatchStartEvent;
import com.lld.cricbuzz.locking.LockManager;
import com.lld.cricbuzz.repository.MatchRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing matches
 * Single Responsibility: Match lifecycle management
 * 
 * Uses LockManager at service layer for thread-safe operations
 * Repositories remain simple data access layers
 */
public class MatchService {
    private final MatchRepository matchRepository;
    private final EventBus eventBus;
    private final LockManager lockManager;

    public MatchService(MatchRepository matchRepository, EventBus eventBus) {
        this.matchRepository = matchRepository;
        this.eventBus = eventBus;
        this.lockManager = LockManager.getInstance();
    }

    public Match createMatch(String tournamentId, MatchFormat format,
                             String team1Id, String team2Id, String venue) {
        String matchId = "MATCH_" + UUID.randomUUID().toString().substring(0, 8);
        Match match = new Match(matchId, tournamentId, format, team1Id, team2Id, venue);
        matchRepository.save(match);
        return match;
    }

    /**
     * Save a match created using the builder pattern
     */
    public Match saveMatch(Match match) {
        matchRepository.save(match);
        return match;
    }

    public void setSquad(String matchId, String teamId, List<String> playerIds) {
        // Use LockManager for thread-safe squad updates
        lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
            match.setSquad(teamId, playerIds);
            matchRepository.save(match);
        });
    }

    public void replacePlayer(String matchId, String teamId, String oldPlayerId, String newPlayerId) {
        lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
            match.replacePlayer(teamId, oldPlayerId, newPlayerId);
            matchRepository.save(match);
        });
    }

    public void recordToss(String matchId, String tossWinnerId, boolean choseBatting) {
        lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
            match.recordToss(tossWinnerId, choseBatting);
            matchRepository.save(match);
        });
    }

    public void startMatch(String matchId) {
        lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
            match.startMatch();
            matchRepository.save(match);
        });
        
        // Publish event outside lock to avoid blocking
        String eventId = "EVT_" + UUID.randomUUID().toString().substring(0, 8);
        eventBus.publish(new MatchStartEvent(eventId, matchId));
    }

    public void addUmpire(String matchId, String umpireId) {
        lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
            match.addUmpire(umpireId);
            matchRepository.save(match);
        });
    }

    public void addScorer(String matchId, String scorerId) {
        lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
            match.addScorer(scorerId);
            matchRepository.save(match);
        });
    }

    public Match getMatch(String matchId) {
        return matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    public List<Match> getMatchesByTournament(String tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }
}

