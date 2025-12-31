package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.match.*;
import com.lld.cricbuzz.domain.player.PlayerMatchStats;
import com.lld.cricbuzz.events.*;
import com.lld.cricbuzz.locking.LockManager;
import com.lld.cricbuzz.repository.MatchRepository;
import com.lld.cricbuzz.repository.PlayerMatchStatsRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service for recording ball-by-ball scoring
 * Single Responsibility: Match scoring operations
 * 
 * Uses LockManager at service layer for thread-safe operations
 * Repositories remain simple data access layers
 */
public class ScoringService {
    private final MatchRepository matchRepository;
    private final PlayerMatchStatsRepository statsRepository;
    private final EventBus eventBus;
    private final LockManager lockManager;

    public ScoringService(MatchRepository matchRepository,
                         PlayerMatchStatsRepository statsRepository,
                         EventBus eventBus) {
        this.matchRepository = matchRepository;
        this.statsRepository = statsRepository;
        this.eventBus = eventBus;
        this.lockManager = LockManager.getInstance();
    }

    /**
     * Records a ball event with thread-safe concurrency handling
     * Uses LockManager at service layer for match-level locking
     * This prevents race conditions when multiple scorers update the same match
     */
    public BallEvent recordBall(String matchId, int overNumber, int ballNumber,
                                String bowlerId, String strikerId, String nonStrikerId,
                                BallOutcome outcome) {
        // Use LockManager at service layer for thread-safe updates
        BallEvent ballEvent = lockManager.executeWithLock("Match", matchId, () -> {
            Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
            if (match.getStatus() != MatchStatus.LIVE) {
                throw new IllegalStateException("Match is not live");
            }

            Innings currentInnings = match.getCurrentInnings();
            if (currentInnings == null) {
                throw new IllegalStateException("No innings in progress");
            }

            String ballEventId = "BALL_" + UUID.randomUUID().toString().substring(0, 8);
            BallEvent event = new BallEvent(ballEventId, currentInnings.getInningsId(),
                overNumber, ballNumber, bowlerId, strikerId, nonStrikerId, outcome);

            // Get or create current over
            Over currentOver = getOrCreateOver(currentInnings, overNumber, bowlerId);
            currentOver.addBall(event);

            // Handle strike rotation
            if (shouldRotateStrike(outcome)) {
                currentInnings.rotateStrike();
            }
            
            // Save match within lock
            matchRepository.save(match);
            
            return event;
        });

        // Update player stats (with separate locking for stats using LockManager)
        updatePlayerStats(matchId, strikerId, bowlerId, outcome);

        // Check for milestones and publish events (outside lock to avoid blocking)
        checkAndPublishEvents(matchId, strikerId, bowlerId, outcome);

        return ballEvent;
    }

    private Over getOrCreateOver(Innings innings, int overNumber, String bowlerId) {
        List<Over> overs = innings.getOvers();
        if (!overs.isEmpty() && overs.get(overs.size() - 1).getOverNumber() == overNumber) {
            return overs.get(overs.size() - 1);
        }
        
        String overId = innings.getInningsId() + "_OVER_" + overNumber;
        Over over = new Over(overId, innings.getInningsId(), overNumber, bowlerId);
        innings.addOver(over);
        return over;
    }

    private boolean shouldRotateStrike(BallOutcome outcome) {
        int runs = outcome.getRuns();
        // Rotate strike on odd runs, unless it's a wide/no-ball
        if (outcome.isExtra() && (outcome.getExtraType() == ExtraType.WIDE || 
                                  outcome.getExtraType() == ExtraType.NO_BALL)) {
            return false;
        }
        return runs % 2 == 1;
    }

    /**
     * Updates player statistics with thread-safe locking
     * Uses LockManager at service layer for per-player-match locks
     */
    private void updatePlayerStats(String matchId, String batterId, String bowlerId, BallOutcome outcome) {
        // Update batter stats with lock using LockManager
        lockManager.executeWithLock("PlayerStats", batterId + ":" + matchId, () -> {
            PlayerMatchStats batterStats = statsRepository.findByPlayerIdAndMatchId(batterId, matchId)
                .orElseGet(() -> {
                    PlayerMatchStats newStats = new PlayerMatchStats(batterId, matchId);
                    statsRepository.save(newStats);
                    return newStats;
                });
            
            if (outcome.isWicket()) {
                batterStats.recordDismissal(outcome.getWicket().getType().name());
            } else {
                int runs = outcome.getRuns();
                if (runs == 4) {
                    batterStats.addFour();
                } else if (runs == 6) {
                    batterStats.addSix();
                } else if (runs > 0) {
                    batterStats.addRuns(runs);
                } else {
                    batterStats.incrementBallsFaced();
                }
            }
            statsRepository.save(batterStats);
            return null;
        });
        
        // Update bowler stats with lock using LockManager
        if (!outcome.isExtra() || outcome.getExtraType() == ExtraType.NO_BALL) {
            lockManager.executeWithLock("PlayerStats", bowlerId + ":" + matchId, () -> {
                PlayerMatchStats bowlerStats = statsRepository.findByPlayerIdAndMatchId(bowlerId, matchId)
                    .orElseGet(() -> {
                        PlayerMatchStats newStats = new PlayerMatchStats(bowlerId, matchId);
                        statsRepository.save(newStats);
                        return newStats;
                    });
                
                if (outcome.isWicket()) {
                    bowlerStats.addWicket(outcome.getWicket().getType().name());
                }
                bowlerStats.addRunsConceded(outcome.getRuns());
                bowlerStats.addBall();
                if (bowlerStats.getBallsBowled() % 6 == 0) {
                    bowlerStats.addOver();
                }
                statsRepository.save(bowlerStats);
                return null;
            });
        }
    }

    private void checkAndPublishEvents(String matchId, String batterId, String bowlerId, BallOutcome outcome) {
        String eventId = "EVT_" + UUID.randomUUID().toString().substring(0, 8);

        // Wicket event
        if (outcome.isWicket()) {
            eventBus.publish(new WicketEvent(eventId, matchId, outcome.getWicket()));
        }

        // Boundary event
        if (outcome.getRuns() == 4 || outcome.getRuns() == 6) {
            eventBus.publish(new BoundaryEvent(eventId, matchId, batterId, outcome.getRuns()));
        }

        // Milestone checks
        PlayerMatchStats batterStats = statsRepository.findByPlayerIdAndMatchId(batterId, matchId)
            .orElse(new PlayerMatchStats(batterId, matchId));
        checkBatterMilestones(eventId, matchId, batterId, batterStats);

        PlayerMatchStats bowlerStats = statsRepository.findByPlayerIdAndMatchId(bowlerId, matchId)
            .orElse(new PlayerMatchStats(bowlerId, matchId));
        checkBowlerMilestones(eventId, matchId, bowlerId, bowlerStats);
    }

    private void checkBatterMilestones(String eventId, String matchId, String playerId, PlayerMatchStats stats) {
        int runs = stats.getRuns();
        MilestoneEvent.MilestoneType type = null;
        if (runs == 50) {
            type = MilestoneEvent.MilestoneType.FIFTY;
        } else if (runs == 100) {
            type = MilestoneEvent.MilestoneType.HUNDRED;
        } else if (runs == 150) {
            type = MilestoneEvent.MilestoneType.HUNDRED_FIFTY;
        } else if (runs == 200) {
            type = MilestoneEvent.MilestoneType.DOUBLE_HUNDRED;
        }
        if (type != null) {
            eventBus.publish(new MilestoneEvent(eventId, matchId, playerId, type, runs));
        }
    }

    private void checkBowlerMilestones(String eventId, String matchId, String playerId, PlayerMatchStats stats) {
        int wickets = stats.getWickets();
        if (wickets == 5) {
            eventBus.publish(new MilestoneEvent(eventId, matchId, playerId,
                MilestoneEvent.MilestoneType.FIVE_WICKET_HAUL, wickets));
        }
        // Hat-trick logic would need to track consecutive wickets
    }

    public void completeInnings(String matchId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        match.completeInnings();
        matchRepository.save(match);
    }
}

