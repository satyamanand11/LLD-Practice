package com.lld.cricbuzz.decorator;

import com.lld.cricbuzz.domain.match.Match;
import com.lld.cricbuzz.domain.player.PlayerMatchStats;
import com.lld.cricbuzz.repository.MatchRepository;
import com.lld.cricbuzz.repository.PlayerMatchStatsRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Decorator that adds live statistics to commentary
 * Enhances commentary with real-time match and player statistics
 */
public class StatisticsDecorator extends CommentaryDecorator {
    private final MatchRepository matchRepository;
    private final PlayerMatchStatsRepository statsRepository;
    private final String playerId; // Optional: specific player to show stats for

    public StatisticsDecorator(CommentaryComponent wrapped,
                              MatchRepository matchRepository,
                              PlayerMatchStatsRepository statsRepository) {
        super(wrapped);
        this.matchRepository = matchRepository;
        this.statsRepository = statsRepository;
        this.playerId = null;
    }

    public StatisticsDecorator(CommentaryComponent wrapped,
                              MatchRepository matchRepository,
                              PlayerMatchStatsRepository statsRepository,
                              String playerId) {
        super(wrapped);
        this.matchRepository = matchRepository;
        this.statsRepository = statsRepository;
        this.playerId = playerId;
    }

    @Override
    public String getEnhancedText() {
        String baseText = wrapped.getEnhancedText();
        String statsText = buildStatisticsText();
        return baseText + " " + statsText;
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>(wrapped.getMetadata());
        metadata.put("statistics", getStatistics());
        return metadata;
    }

    private String buildStatisticsText() {
        StringBuilder stats = new StringBuilder("[Stats: ");
        
        Optional<Match> matchOpt = matchRepository.findById(getMatchId());
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            var currentInnings = match.getCurrentInnings();
            if (currentInnings != null) {
                stats.append("Score: ").append(currentInnings.getTotalRuns())
                     .append("/").append(currentInnings.getTotalWickets())
                     .append(" (").append(currentInnings.getCurrentOverNumber())
                     .append(" overs)");
            }
        }

        // Add player-specific stats if playerId is provided
        if (playerId != null) {
            Optional<PlayerMatchStats> playerStatsOpt = statsRepository
                .findByPlayerIdAndMatchId(playerId, getMatchId());
            if (playerStatsOpt.isPresent()) {
                PlayerMatchStats playerStats = playerStatsOpt.get();
                if (playerStats.getRuns() > 0 || playerStats.getBallsFaced() > 0) {
                    stats.append(" | Batter: ").append(playerStats.getRuns())
                         .append("(").append(playerStats.getBallsFaced()).append(")");
                }
                if (playerStats.getWickets() > 0) {
                    stats.append(" | Bowler: ").append(playerStats.getWickets())
                         .append("/").append(playerStats.getRunsConceded());
                }
            }
        }

        stats.append("]");
        return stats.toString();
    }

    private Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Optional<Match> matchOpt = matchRepository.findById(getMatchId());
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            var currentInnings = match.getCurrentInnings();
            if (currentInnings != null) {
                stats.put("runs", currentInnings.getTotalRuns());
                stats.put("wickets", currentInnings.getTotalWickets());
                stats.put("overs", currentInnings.getCurrentOverNumber());
            }
        }

        if (playerId != null) {
            Optional<PlayerMatchStats> playerStats = statsRepository
                .findByPlayerIdAndMatchId(playerId, getMatchId());
            if (playerStats.isPresent()) {
                PlayerMatchStats ps = playerStats.get();
                Map<String, Object> playerStatsMap = new HashMap<>();
                playerStatsMap.put("runs", ps.getRuns());
                playerStatsMap.put("ballsFaced", ps.getBallsFaced());
                playerStatsMap.put("wickets", ps.getWickets());
                playerStatsMap.put("runsConceded", ps.getRunsConceded());
                stats.put("player", playerStatsMap);
            }
        }

        return stats;
    }
}

