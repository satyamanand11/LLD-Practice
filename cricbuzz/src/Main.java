import com.lld.cricbuzz.domain.commentary.CommentaryType;
import com.lld.cricbuzz.domain.match.*;
import com.lld.cricbuzz.domain.notifications.SubscriptionType;
import com.lld.cricbuzz.domain.player.Player;
import com.lld.cricbuzz.domain.player.PlayerRole;
import com.lld.cricbuzz.domain.team.Team;
import com.lld.cricbuzz.domain.tournament.MatchFormat;
import com.lld.cricbuzz.domain.tournament.Tournament;
import com.lld.cricbuzz.facade.CricbuzzSystem;
import com.lld.cricbuzz.facade.CricbuzzSystemImpl;
import com.lld.cricbuzz.service.ScorecardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Main class demonstrating Cricbuzz Live Cricket Scoring System
 * 
 * This demonstrates:
 * - DDD (Domain-Driven Design) with aggregate roots
 * - SOLID principles
 * - Design Patterns: Facade, Builder, Strategy, Observer, Factory, Command, Decorator
 * - Complete match scoring workflow
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Cricbuzz Live Cricket Scoring System ===\n");

        // Get singleton facade instance (Facade Pattern)
        CricbuzzSystem system = CricbuzzSystemImpl.getInstance();
        System.out.println("✓ Using Facade Pattern: CricbuzzSystem\n");

        // ========== DEMONSTRATION ==========

        System.out.println("1. Creating Tournament using Builder Pattern");
        // Using Builder Pattern for tournament creation
        Tournament tournament = system.buildTournament()
            .setName("ICC World Cup 2024")
            .setFormat(MatchFormat.ODI)
            .setDates(LocalDate.now(), LocalDate.now().plusDays(30))
            .setInitialStatus(com.lld.cricbuzz.domain.tournament.TournamentStatus.SCHEDULED)
            .build();
        
        // Save tournament through facade
        tournament = system.saveTournament(tournament);
        System.out.println("   Created: " + tournament.getName() + " (ID: " + tournament.getTournamentId() + ")");
        System.out.println("   Format: " + tournament.getFormat().getDisplayName());
        System.out.println("   Dates: " + tournament.getStartDate() + " to " + tournament.getEndDate() + "\n");

        System.out.println("2. Creating Teams");
        Team team1 = system.createTeam("India", "India");
        Team team2 = system.createTeam("Australia", "Australia");
        System.out.println("   Team 1: " + team1.getName() + " (ID: " + team1.getTeamId() + ")");
        System.out.println("   Team 2: " + team2.getName() + " (ID: " + team2.getTeamId() + ")\n");

        // Add teams to tournament
        system.addTeamToTournament(tournament.getTournamentId(), team1.getTeamId());
        system.addTeamToTournament(tournament.getTournamentId(), team2.getTeamId());

        System.out.println("3. Creating Players");
        // India players
        Player kohli = system.createPlayer("Virat Kohli", 35, "India", PlayerRole.BATTER);
        Player rohit = system.createPlayer("Rohit Sharma", 37, "India", PlayerRole.BATTER);
        Player bumrah = system.createPlayer("Jasprit Bumrah", 30, "India", PlayerRole.BOWLER);
        Player pandya = system.createPlayer("Hardik Pandya", 30, "India", PlayerRole.ALL_ROUNDER);
        Player dhoni = system.createPlayer("MS Dhoni", 42, "India", PlayerRole.WICKET_KEEPER);

        // Australia players
        Player smith = system.createPlayer("Steve Smith", 35, "Australia", PlayerRole.BATTER);
        Player warner = system.createPlayer("David Warner", 37, "Australia", PlayerRole.BATTER);
        Player starc = system.createPlayer("Mitchell Starc", 34, "Australia", PlayerRole.BOWLER);
        Player cummins = system.createPlayer("Pat Cummins", 31, "Australia", PlayerRole.BOWLER);
        Player carey = system.createPlayer("Alex Carey", 32, "Australia", PlayerRole.WICKET_KEEPER);

        System.out.println("   Created players for both teams\n");

        // Add players to teams
        system.addPlayerToTeam(team1.getTeamId(), kohli.getPlayerId());
        system.addPlayerToTeam(team1.getTeamId(), rohit.getPlayerId());
        system.addPlayerToTeam(team1.getTeamId(), bumrah.getPlayerId());
        system.addPlayerToTeam(team1.getTeamId(), pandya.getPlayerId());
        system.addPlayerToTeam(team1.getTeamId(), dhoni.getPlayerId());

        system.addPlayerToTeam(team2.getTeamId(), smith.getPlayerId());
        system.addPlayerToTeam(team2.getTeamId(), warner.getPlayerId());
        system.addPlayerToTeam(team2.getTeamId(), starc.getPlayerId());
        system.addPlayerToTeam(team2.getTeamId(), cummins.getPlayerId());
        system.addPlayerToTeam(team2.getTeamId(), carey.getPlayerId());

        System.out.println("4. Creating Match using Builder Pattern");
        // Using Builder Pattern for match creation
        Match match = system.buildMatch()
            .setTournament(tournament.getTournamentId())
            .setFormat(MatchFormat.ODI)
            .setTeams(team1.getTeamId(), team2.getTeamId())
            .setVenue("Wankhede Stadium, Mumbai")
            .setScheduledTime(LocalDateTime.now().plusDays(1))
            .addUmpire("UMP_001")
            .addUmpire("UMP_002")
            .addScorer("SCR_001")
            .build();
        
        // Save match through facade (also adds to tournament)
        match = system.saveMatch(match);
        System.out.println("   Match ID: " + match.getMatchId());
        System.out.println("   Venue: " + match.getVenue());
        System.out.println("   Format: " + match.getFormat().getDisplayName());
        System.out.println("   Scheduled: " + match.getScheduledTime());
        System.out.println("   Umpires: " + match.getUmpireIds().size());
        System.out.println("   Scorers: " + match.getScorerIds().size() + "\n");

        System.out.println("5. Setting Squads");
        List<String> indiaSquad = Arrays.asList(
            kohli.getPlayerId(), rohit.getPlayerId(), bumrah.getPlayerId(),
            pandya.getPlayerId(), dhoni.getPlayerId()
        );
        List<String> ausSquad = Arrays.asList(
            smith.getPlayerId(), warner.getPlayerId(), starc.getPlayerId(),
            cummins.getPlayerId(), carey.getPlayerId()
        );
        system.setMatchSquad(match.getMatchId(), team1.getTeamId(), indiaSquad);
        system.setMatchSquad(match.getMatchId(), team2.getTeamId(), ausSquad);
        System.out.println("   Squads set for both teams\n");

        System.out.println("6. Recording Toss");
        system.recordToss(match.getMatchId(), team1.getTeamId(), true);
        System.out.println("   " + team1.getName() + " won the toss and chose to bat\n");

        System.out.println("7. Starting Match");
        system.startMatch(match.getMatchId());
        System.out.println("   Match is now LIVE!\n");

        System.out.println("8. User Subscriptions (Observer Pattern)");
        system.subscribe("USER_001", SubscriptionType.MATCH, match.getMatchId());
        system.subscribe("USER_002", SubscriptionType.PLAYER, kohli.getPlayerId());
        system.subscribe("USER_003", SubscriptionType.TEAM, team1.getTeamId());
        System.out.println("   Users subscribed to match, player, and team updates\n");

        System.out.println("9. Recording Ball-by-Ball Scoring");
        System.out.println("    === First Over ===\n");

        // First over - India batting
        String striker = rohit.getPlayerId();
        String nonStriker = kohli.getPlayerId();
        String bowler = starc.getPlayerId();

        // Ball 1: 4 runs
        BallOutcome ball1 = BallOutcome.regularRuns(4);
        BallEvent event1 = system.recordBall(match.getMatchId(), 1, 1, bowler, striker, nonStriker, ball1);
        system.addCommentary(match.getMatchId(), event1.getBallEventId(), CommentaryType.MANUAL,
            "Rohit Sharma hits a beautiful cover drive for FOUR!", "COMM_001");
        System.out.println("    Ball 1.1: " + striker + " scores 4 runs");

        // Ball 2: 1 run
        BallOutcome ball2 = BallOutcome.regularRuns(1);
        system.recordBall(match.getMatchId(), 1, 2, bowler, striker, nonStriker, ball2);
        System.out.println("    Ball 1.2: " + striker + " scores 1 run (strike rotates)");

        // Ball 3: 6 runs
        BallOutcome ball3 = BallOutcome.regularRuns(6);
        BallEvent event3 = system.recordBall(match.getMatchId(), 1, 3, bowler, nonStriker, striker, ball3);
        system.addCommentary(match.getMatchId(), event3.getBallEventId(), CommentaryType.MANUAL,
            "Kohli smashes it over long-on for SIX!", "COMM_001");
        System.out.println("    Ball 1.3: " + nonStriker + " scores 6 runs");

        // Ball 4: Wicket
        Wicket wicket = new Wicket("WKT_001", striker, WicketType.CAUGHT, bowler,
            carey.getPlayerId(), 1, 4, 5, 2);
        BallOutcome ball4 = BallOutcome.wicket(wicket);
        BallEvent event4 = system.recordBall(match.getMatchId(), 1, 4, bowler, striker, nonStriker, ball4);
        system.addCommentary(match.getMatchId(), event4.getBallEventId(), CommentaryType.WICKET,
            "WICKET! Rohit Sharma caught by Alex Carey", "COMM_001");
        System.out.println("    Ball 1.4: WICKET! " + striker + " dismissed");

        // Ball 5: 2 runs
        String newStriker = pandya.getPlayerId();
        BallOutcome ball5 = BallOutcome.regularRuns(2);
        system.recordBall(match.getMatchId(), 1, 5, bowler, newStriker, nonStriker, ball5);
        System.out.println("    Ball 1.5: " + newStriker + " scores 2 runs");

        // Ball 6: 0 runs
        BallOutcome ball6 = BallOutcome.regularRuns(0);
        system.recordBall(match.getMatchId(), 1, 6, bowler, newStriker, nonStriker, ball6);
        System.out.println("    Ball 1.6: Dot ball\n");

        System.out.println("10. Generating Scorecard");
        ScorecardService.Scorecard scorecard = system.getScorecard(match.getMatchId());
        System.out.println("    Match: " + scorecard.getTeam1Id() + " vs " + scorecard.getTeam2Id());
        System.out.println("    Status: " + scorecard.getStatus());
        if (!scorecard.getInnings().isEmpty()) {
            ScorecardService.InningsScorecard inn1 = scorecard.getInnings().get(0);
            System.out.println("    Innings 1: " + inn1.getTotalRuns() + "/" + inn1.getTotalWickets() +
                " (" + inn1.getOvers() + "." + inn1.getBalls() + " overs)");
        }
        System.out.println();

        System.out.println("11. Viewing Commentary");
        List<com.lld.cricbuzz.domain.commentary.Commentary> commentaries =
            system.getMatchCommentary(match.getMatchId());
        for (com.lld.cricbuzz.domain.commentary.Commentary comm : commentaries) {
            System.out.println("    [" + comm.getTimestamp() + "] " + comm.getText());
        }
        System.out.println();

        System.out.println("=== Demo Complete ===");
        System.out.println("\nKey Features Demonstrated:");
        System.out.println("✓ Tournament and Match Management");
        System.out.println("✓ Team and Player Management");
        System.out.println("✓ Match Setup (Squads, Toss)");
        System.out.println("✓ Ball-by-Ball Scoring");
        System.out.println("✓ Statistics Tracking");
        System.out.println("✓ Commentary System");
        System.out.println("✓ Notification System (Observer Pattern)");
        System.out.println("✓ Scorecard Generation");
        System.out.println("\nDesign Patterns Used:");
        System.out.println("✓ Facade Pattern: Unified interface through CricbuzzSystem");
        System.out.println("✓ Builder Pattern: TournamentBuilder and MatchBuilder for step-by-step construction");
        System.out.println("✓ Strategy Pattern: Scoring strategies for different formats");
        System.out.println("✓ Observer Pattern: Event-driven notifications");
        System.out.println("✓ Factory Pattern: Strategy creation");
        System.out.println("✓ Repository Pattern: Data access abstraction");
        System.out.println("✓ Command Pattern: Encapsulated user operations");
        System.out.println("✓ Decorator Pattern: Commentary enhancements");
        System.out.println("✓ Singleton Pattern: Thread-safe facade instance");
        System.out.println("\nSOLID Principles:");
        System.out.println("✓ Single Responsibility: Each service has one clear purpose");
        System.out.println("✓ Open/Closed: Extensible via Strategy pattern");
        System.out.println("✓ Liskov Substitution: Repository implementations are interchangeable");
        System.out.println("✓ Interface Segregation: Focused repository interfaces");
        System.out.println("✓ Dependency Inversion: Services depend on repository interfaces");
    }
}
