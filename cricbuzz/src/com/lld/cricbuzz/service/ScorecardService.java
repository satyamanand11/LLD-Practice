package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.match.*;
import com.lld.cricbuzz.domain.player.PlayerMatchStats;
import com.lld.cricbuzz.repository.MatchRepository;
import com.lld.cricbuzz.repository.PlayerMatchStatsRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating scorecards
 * Single Responsibility: Scorecard generation and statistics
 */
public class ScorecardService {
    private final MatchRepository matchRepository;
    private final PlayerMatchStatsRepository statsRepository;

    public ScorecardService(MatchRepository matchRepository,
                           PlayerMatchStatsRepository statsRepository) {
        this.matchRepository = matchRepository;
        this.statsRepository = statsRepository;
    }

    public Scorecard getScorecard(String matchId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        
        List<Innings> innings = match.getInnings();
        List<InningsScorecard> inningsScorecards = new ArrayList<>();
        
        for (Innings innings1 : innings) {
            inningsScorecards.add(buildInningsScorecard(innings1, matchId));
        }
        
        return new Scorecard(matchId, match.getTeam1Id(), match.getTeam2Id(),
            inningsScorecards, match.getStatus());
    }

    private InningsScorecard buildInningsScorecard(Innings innings, String matchId) {
        int totalRuns = innings.getTotalRuns();
        int totalWickets = innings.getTotalWickets();
        int totalOvers = innings.getCurrentOverNumber();
        int totalBalls = innings.getTotalBalls();
        
        // Batting scorecard
        List<BattingScorecard> battingScorecards = buildBattingScorecard(
            innings.getBattingTeamId(), matchId);
        
        // Bowling scorecard
        List<BowlingScorecard> bowlingScorecards = buildBowlingScorecard(
            innings.getBowlingTeamId(), matchId);
        
        // Fall of wickets
        List<FallOfWicket> fallOfWickets = buildFallOfWickets(innings);
        
        return new InningsScorecard(innings.getInningsNumber(), innings.getBattingTeamId(),
            totalRuns, totalWickets, totalOvers, totalBalls, battingScorecards,
            bowlingScorecards, fallOfWickets);
    }

    private List<BattingScorecard> buildBattingScorecard(String teamId, String matchId) {
        List<PlayerMatchStats> allStats = statsRepository.findByMatchId(matchId);
        List<BattingScorecard> scorecards = new ArrayList<>();
        
        for (PlayerMatchStats stats : allStats) {
            // Filter by team (would need team info from player)
            scorecards.add(new BattingScorecard(stats.getPlayerId(), stats.getRuns(),
                stats.getBallsFaced(), stats.getFours(), stats.getSixes(),
                stats.getStrikeRate(), stats.isOut(), stats.getDismissalType()));
        }
        
        return scorecards;
    }

    private List<BowlingScorecard> buildBowlingScorecard(String teamId, String matchId) {
        List<PlayerMatchStats> allStats = statsRepository.findByMatchId(matchId);
        List<BowlingScorecard> scorecards = new ArrayList<>();
        
        for (PlayerMatchStats stats : allStats) {
            if (stats.getBallsBowled() > 0) {
                scorecards.add(new BowlingScorecard(stats.getPlayerId(),
                    stats.getOversBowled(), stats.getBallsBowled() % 6,
                    stats.getMaidens(), stats.getRunsConceded(), stats.getWickets(),
                    stats.getEconomyRate(), stats.getBowlingAverage()));
            }
        }
        
        return scorecards;
    }

    private List<FallOfWicket> buildFallOfWickets(Innings innings) {
        List<FallOfWicket> fallOfWickets = new ArrayList<>();
        int runningScore = 0;
        
        for (Over over : innings.getOvers()) {
            for (BallEvent ball : over.getBalls()) {
                if (ball.getOutcome().isWicket()) {
                    Wicket wicket = ball.getOutcome().getWicket();
                    runningScore += ball.getOutcome().getRuns();
                    fallOfWickets.add(new FallOfWicket(wicket.getBatterId(),
                        runningScore, wicket.getRunsAtWicket(), over.getOverNumber(),
                        ball.getBallNumber()));
                } else {
                    runningScore += ball.getOutcome().getRuns();
                }
            }
        }
        
        return fallOfWickets;
    }

    // DTOs for scorecard
    public static class Scorecard {
        private final String matchId;
        private final String team1Id;
        private final String team2Id;
        private final List<InningsScorecard> innings;
        private final MatchStatus status;

        public Scorecard(String matchId, String team1Id, String team2Id,
                        List<InningsScorecard> innings, MatchStatus status) {
            this.matchId = matchId;
            this.team1Id = team1Id;
            this.team2Id = team2Id;
            this.innings = innings;
            this.status = status;
        }

        // Getters
        public String getMatchId() { return matchId; }
        public String getTeam1Id() { return team1Id; }
        public String getTeam2Id() { return team2Id; }
        public List<InningsScorecard> getInnings() { return innings; }
        public MatchStatus getStatus() { return status; }
    }

    public static class InningsScorecard {
        private final int inningsNumber;
        private final String battingTeamId;
        private final int totalRuns;
        private final int totalWickets;
        private final int overs;
        private final int balls;
        private final List<BattingScorecard> batting;
        private final List<BowlingScorecard> bowling;
        private final List<FallOfWicket> fallOfWickets;

        public InningsScorecard(int inningsNumber, String battingTeamId, int totalRuns,
                               int totalWickets, int overs, int balls,
                               List<BattingScorecard> batting, List<BowlingScorecard> bowling,
                               List<FallOfWicket> fallOfWickets) {
            this.inningsNumber = inningsNumber;
            this.battingTeamId = battingTeamId;
            this.totalRuns = totalRuns;
            this.totalWickets = totalWickets;
            this.overs = overs;
            this.balls = balls;
            this.batting = batting;
            this.bowling = bowling;
            this.fallOfWickets = fallOfWickets;
        }

        // Getters
        public int getInningsNumber() { return inningsNumber; }
        public String getBattingTeamId() { return battingTeamId; }
        public int getTotalRuns() { return totalRuns; }
        public int getTotalWickets() { return totalWickets; }
        public int getOvers() { return overs; }
        public int getBalls() { return balls; }
        public List<BattingScorecard> getBatting() { return batting; }
        public List<BowlingScorecard> getBowling() { return bowling; }
        public List<FallOfWicket> getFallOfWickets() { return fallOfWickets; }
    }

    public static class BattingScorecard {
        private final String playerId;
        private final int runs;
        private final int balls;
        private final int fours;
        private final int sixes;
        private final double strikeRate;
        private final boolean isOut;
        private final String dismissalType;

        public BattingScorecard(String playerId, int runs, int balls, int fours, int sixes,
                               double strikeRate, boolean isOut, String dismissalType) {
            this.playerId = playerId;
            this.runs = runs;
            this.balls = balls;
            this.fours = fours;
            this.sixes = sixes;
            this.strikeRate = strikeRate;
            this.isOut = isOut;
            this.dismissalType = dismissalType;
        }

        // Getters
        public String getPlayerId() { return playerId; }
        public int getRuns() { return runs; }
        public int getBalls() { return balls; }
        public int getFours() { return fours; }
        public int getSixes() { return sixes; }
        public double getStrikeRate() { return strikeRate; }
        public boolean isOut() { return isOut; }
        public String getDismissalType() { return dismissalType; }
    }

    public static class BowlingScorecard {
        private final String playerId;
        private final int overs;
        private final int balls;
        private final int maidens;
        private final int runs;
        private final int wickets;
        private final double economy;
        private final double average;

        public BowlingScorecard(String playerId, int overs, int balls, int maidens,
                               int runs, int wickets, double economy, double average) {
            this.playerId = playerId;
            this.overs = overs;
            this.balls = balls;
            this.maidens = maidens;
            this.runs = runs;
            this.wickets = wickets;
            this.economy = economy;
            this.average = average;
        }

        // Getters
        public String getPlayerId() { return playerId; }
        public int getOvers() { return overs; }
        public int getBalls() { return balls; }
        public int getMaidens() { return maidens; }
        public int getRuns() { return runs; }
        public int getWickets() { return wickets; }
        public double getEconomy() { return economy; }
        public double getAverage() { return average; }
    }

    public static class FallOfWicket {
        private final String playerId;
        private final int score;
        private final int runs;
        private final int over;
        private final int ball;

        public FallOfWicket(String playerId, int score, int runs, int over, int ball) {
            this.playerId = playerId;
            this.score = score;
            this.runs = runs;
            this.over = over;
            this.ball = ball;
        }

        // Getters
        public String getPlayerId() { return playerId; }
        public int getScore() { return score; }
        public int getRuns() { return runs; }
        public int getOver() { return over; }
        public int getBall() { return ball; }
    }
}

