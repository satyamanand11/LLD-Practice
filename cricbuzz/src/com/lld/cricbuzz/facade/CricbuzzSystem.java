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
import com.lld.cricbuzz.command.Command;
import com.lld.cricbuzz.command.CommandResult;
import com.lld.cricbuzz.service.ScorecardService;

import java.util.List;

/**
 * Facade interface for Cricbuzz Live Cricket Scoring System
 * Provides a simplified, unified interface to the complex subsystem
 * 
 * This facade encapsulates:
 * - Tournament management
 * - Team and Player management
 * - Match lifecycle management
 * - Ball-by-ball scoring
 * - Commentary system
 * - Notification subscriptions
 * - Scorecard generation
 */
public interface CricbuzzSystem {
    
    // ========== Tournament Management ==========
    
    /**
     * Create a new tournament
     */
    Tournament createTournament(String name, MatchFormat format);
    
    /**
     * Create tournament using builder pattern
     * Returns TournamentBuilder for step-by-step construction
     */
    com.lld.cricbuzz.builder.TournamentBuilder buildTournament();
    
    /**
     * Save a tournament created using the builder pattern
     */
    Tournament saveTournament(com.lld.cricbuzz.domain.tournament.Tournament tournament);
    
    /**
     * Add a team to a tournament
     */
    void addTeamToTournament(String tournamentId, String teamId);
    
    /**
     * Get tournament by ID
     */
    Tournament getTournament(String tournamentId);
    
    // ========== Team Management ==========
    
    /**
     * Create a new team
     */
    Team createTeam(String name, String country);
    
    /**
     * Get team by ID
     */
    Team getTeam(String teamId);
    
    // ========== Player Management ==========
    
    /**
     * Create a new player
     */
    Player createPlayer(String name, int age, String country, PlayerRole role);
    
    /**
     * Add player to a team
     */
    void addPlayerToTeam(String teamId, String playerId);
    
    /**
     * Get player by ID
     */
    Player getPlayer(String playerId);
    
    // ========== Match Management ==========
    
    /**
     * Create a new match
     */
    Match createMatch(String tournamentId, MatchFormat format, 
                     String team1Id, String team2Id, String venue);
    
    /**
     * Create match using builder pattern
     * Returns MatchBuilder for step-by-step construction
     */
    com.lld.cricbuzz.builder.MatchBuilder buildMatch();
    
    /**
     * Save a match created using the builder pattern
     * Also adds the match to the tournament
     */
    Match saveMatch(com.lld.cricbuzz.domain.match.Match match);
    
    /**
     * Set squad for a team in a match
     */
    void setMatchSquad(String matchId, String teamId, List<String> playerIds);
    
    /**
     * Replace a player in match squad (before match starts)
     */
    void replacePlayerInSquad(String matchId, String teamId, 
                              String oldPlayerId, String newPlayerId);
    
    /**
     * Record toss result
     */
    void recordToss(String matchId, String tossWinnerId, boolean choseBatting);
    
    /**
     * Start the match
     */
    void startMatch(String matchId);
    
    /**
     * Add umpire to match
     */
    void addUmpire(String matchId, String umpireId);
    
    /**
     * Add scorer to match
     */
    void addScorer(String matchId, String scorerId);
    
    /**
     * Get match by ID
     */
    Match getMatch(String matchId);
    
    // ========== Scoring Operations ==========
    
    /**
     * Record a ball event
     */
    BallEvent recordBall(String matchId, int overNumber, int ballNumber,
                        String bowlerId, String strikerId, String nonStrikerId,
                        BallOutcome outcome);
    
    /**
     * Complete current innings
     */
    void completeInnings(String matchId);
    
    // ========== Commentary ==========
    
    /**
     * Add commentary for a match
     */
    Commentary addCommentary(String matchId, String ballEventId,
                            CommentaryType type, String text, String commentatorId);
    
    /**
     * Get all commentary for a match
     */
    List<Commentary> getMatchCommentary(String matchId);
    
    // ========== Notifications ==========
    
    /**
     * Subscribe to match/team/player updates
     */
    Subscription subscribe(String userId, SubscriptionType type, String entityId);
    
    /**
     * Unsubscribe from updates
     */
    void unsubscribe(String subscriptionId);
    
    /**
     * Get user subscriptions
     */
    List<Subscription> getUserSubscriptions(String userId);
    
    // ========== Scorecard ==========
    
    /**
     * Get live scorecard for a match
     */
    ScorecardService.Scorecard getScorecard(String matchId);
    
    // ========== Command Pattern Operations ==========
    
    /**
     * Execute a command (Command Pattern)
     */
    CommandResult executeCommand(Command command);
    
    /**
     * Undo last command
     */
    CommandResult undoLastCommand();
    
    /**
     * Redo last undone command
     */
    CommandResult redoLastCommand();
    
    /**
     * Get command history
     */
    List<Command> getCommandHistory();
    
    /**
     * Get command history for a specific executor
     */
    List<Command> getCommandHistoryByExecutor(String executorId);
}

