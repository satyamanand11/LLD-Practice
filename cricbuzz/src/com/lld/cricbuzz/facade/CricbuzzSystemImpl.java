package com.lld.cricbuzz.facade;

import com.lld.cricbuzz.domain.commentary.Commentary;
import com.lld.cricbuzz.domain.commentary.CommentaryType;
import com.lld.cricbuzz.domain.match.*;
import com.lld.cricbuzz.domain.notifications.Subscription;
import com.lld.cricbuzz.domain.notifications.SubscriptionType;
import com.lld.cricbuzz.domain.player.Player;
import com.lld.cricbuzz.domain.player.PlayerRole;
import com.lld.cricbuzz.domain.team.Team;
import com.lld.cricbuzz.domain.tournament.MatchFormat;
import com.lld.cricbuzz.domain.tournament.Tournament;
import com.lld.cricbuzz.events.EventBus;
import com.lld.cricbuzz.command.Command;
import com.lld.cricbuzz.command.CommandInvoker;
import com.lld.cricbuzz.command.CommandResult;
import com.lld.cricbuzz.repository.*;
import com.lld.cricbuzz.repository.impl.*;
import com.lld.cricbuzz.service.*;

import java.util.List;

/**
 * Singleton, thread-safe implementation of CricbuzzSystem facade
 * 
 * Thread Safety:
 * - Singleton instance is created using double-checked locking
 * - All underlying services are stateless or thread-safe
 * - Repository operations are thread-safe
 */
public class CricbuzzSystemImpl implements CricbuzzSystem {
    
    // Singleton instance with double-checked locking
    private static volatile CricbuzzSystemImpl instance;
    private static final Object lock = new Object();
    
    // Services - all are stateless and thread-safe
    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final ScoringService scoringService;
    private final CommentaryService commentaryService;
    private final NotificationService notificationService;
    private final ScorecardService scorecardService;
    
    // Command Pattern
    private final CommandInvoker commandInvoker;
    
    // EventBus for observer pattern
    private final EventBus eventBus;
    
    /**
     * Private constructor - prevents instantiation
     * Initializes all services and repositories
     */
    private CricbuzzSystemImpl() {
        // Initialize repositories
        TournamentRepository tournamentRepository = new InMemoryTournamentRepository();
        MatchRepository matchRepository = new InMemoryMatchRepository();
        TeamRepository teamRepository = new InMemoryTeamRepository();
        PlayerRepository playerRepository = new InMemoryPlayerRepository();
        CommentaryRepository commentaryRepository = new InMemoryCommentaryRepository();
        SubscriptionRepository subscriptionRepository = new InMemorySubscriptionRepository();
        PlayerMatchStatsRepository statsRepository = new InMemoryPlayerMatchStatsRepository();
        
        // Initialize EventBus (Singleton)
        eventBus = EventBus.getInstance();
        
        // Initialize services
        tournamentService = new TournamentService(tournamentRepository);
        teamService = new TeamService(teamRepository);
        playerService = new PlayerService(playerRepository);
        matchService = new MatchService(matchRepository, eventBus);
        scoringService = new ScoringService(matchRepository, statsRepository, eventBus);
        commentaryService = new CommentaryService(commentaryRepository);
        notificationService = new NotificationService(subscriptionRepository);
        scorecardService = new ScorecardService(matchRepository, statsRepository);
        
        // Initialize Command Invoker
        commandInvoker = new CommandInvoker();
        
        // Subscribe notification service to events
        eventBus.subscribe(notificationService);
    }
    
    /**
     * Get singleton instance using double-checked locking pattern
     * Thread-safe singleton implementation
     */
    public static CricbuzzSystemImpl getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CricbuzzSystemImpl();
                }
            }
        }
        return instance;
    }
    
    // ========== Tournament Management ==========
    
    @Override
    public Tournament createTournament(String name, MatchFormat format) {
        return tournamentService.createTournament(name, format);
    }
    
    @Override
    public com.lld.cricbuzz.builder.TournamentBuilder buildTournament() {
        return new com.lld.cricbuzz.builder.TournamentBuilder();
    }
    
    @Override
    public Tournament saveTournament(Tournament tournament) {
        return tournamentService.saveTournament(tournament);
    }
    
    @Override
    public void addTeamToTournament(String tournamentId, String teamId) {
        tournamentService.addTeam(tournamentId, teamId);
    }
    
    @Override
    public Tournament getTournament(String tournamentId) {
        return tournamentService.getTournament(tournamentId);
    }
    
    // ========== Team Management ==========
    
    @Override
    public Team createTeam(String name, String country) {
        return teamService.createTeam(name, country);
    }
    
    @Override
    public Team getTeam(String teamId) {
        return teamService.getTeam(teamId);
    }
    
    // ========== Player Management ==========
    
    @Override
    public Player createPlayer(String name, int age, String country, PlayerRole role) {
        return playerService.createPlayer(name, age, country, role);
    }
    
    @Override
    public void addPlayerToTeam(String teamId, String playerId) {
        teamService.addPlayer(teamId, playerId);
    }
    
    @Override
    public Player getPlayer(String playerId) {
        return playerService.getPlayer(playerId);
    }
    
    // ========== Match Management ==========
    
    @Override
    public Match createMatch(String tournamentId, MatchFormat format,
                            String team1Id, String team2Id, String venue) {
        Match match = matchService.createMatch(tournamentId, format, team1Id, team2Id, venue);
        tournamentService.scheduleMatch(tournamentId, match.getMatchId());
        return match;
    }
    
    @Override
    public com.lld.cricbuzz.builder.MatchBuilder buildMatch() {
        return new com.lld.cricbuzz.builder.MatchBuilder();
    }
    
    @Override
    public Match saveMatch(Match match) {
        Match savedMatch = matchService.saveMatch(match);
        // Also add to tournament
        tournamentService.scheduleMatch(match.getTournamentId(), savedMatch.getMatchId());
        return savedMatch;
    }
    
    @Override
    public void setMatchSquad(String matchId, String teamId, List<String> playerIds) {
        matchService.setSquad(matchId, teamId, playerIds);
    }
    
    @Override
    public void replacePlayerInSquad(String matchId, String teamId,
                                    String oldPlayerId, String newPlayerId) {
        matchService.replacePlayer(matchId, teamId, oldPlayerId, newPlayerId);
    }
    
    @Override
    public void recordToss(String matchId, String tossWinnerId, boolean choseBatting) {
        matchService.recordToss(matchId, tossWinnerId, choseBatting);
    }
    
    @Override
    public void startMatch(String matchId) {
        matchService.startMatch(matchId);
    }
    
    @Override
    public void addUmpire(String matchId, String umpireId) {
        matchService.addUmpire(matchId, umpireId);
    }
    
    @Override
    public void addScorer(String matchId, String scorerId) {
        matchService.addScorer(matchId, scorerId);
    }
    
    @Override
    public Match getMatch(String matchId) {
        return matchService.getMatch(matchId);
    }
    
    // ========== Scoring Operations ==========
    
    @Override
    public BallEvent recordBall(String matchId, int overNumber, int ballNumber,
                               String bowlerId, String strikerId, String nonStrikerId,
                               BallOutcome outcome) {
        return scoringService.recordBall(matchId, overNumber, ballNumber,
                                        bowlerId, strikerId, nonStrikerId, outcome);
    }
    
    @Override
    public void completeInnings(String matchId) {
        scoringService.completeInnings(matchId);
    }
    
    // ========== Commentary ==========
    
    @Override
    public Commentary addCommentary(String matchId, String ballEventId,
                                   CommentaryType type, String text, String commentatorId) {
        return commentaryService.addCommentary(matchId, ballEventId, type, text, commentatorId);
    }
    
    @Override
    public List<Commentary> getMatchCommentary(String matchId) {
        return commentaryService.getCommentaryByMatch(matchId);
    }
    
    // ========== Notifications ==========
    
    @Override
    public Subscription subscribe(String userId, SubscriptionType type, String entityId) {
        return notificationService.subscribe(userId, type, entityId);
    }
    
    @Override
    public void unsubscribe(String subscriptionId) {
        notificationService.unsubscribe(subscriptionId);
    }
    
    @Override
    public List<Subscription> getUserSubscriptions(String userId) {
        return notificationService.getUserSubscriptions(userId);
    }
    
    // ========== Scorecard ==========
    
    @Override
    public ScorecardService.Scorecard getScorecard(String matchId) {
        return scorecardService.getScorecard(matchId);
    }
    
    // ========== Command Pattern Operations ==========
    
    @Override
    public CommandResult executeCommand(Command command) {
        return commandInvoker.execute(command);
    }
    
    @Override
    public CommandResult undoLastCommand() {
        return commandInvoker.undo();
    }
    
    @Override
    public CommandResult redoLastCommand() {
        return commandInvoker.redo();
    }
    
    @Override
    public List<Command> getCommandHistory() {
        return commandInvoker.getHistory();
    }
    
    @Override
    public List<Command> getCommandHistoryByExecutor(String executorId) {
        return commandInvoker.getHistoryByExecutor(executorId);
    }
}

