package com.lld.connect4.service.facade;

import com.lld.connect4.domain.board.Board;
import com.lld.connect4.domain.game.*;
import com.lld.connect4.domain.rules.WinChecker;
import com.lld.connect4.repo.GameRepository;
import com.lld.connect4.repo.InMemoryGameRepository;
import com.lld.connect4.service.GameService;
import com.lld.connect4.service.LockManager;

/**
 * Singleton, thread-safe implementation of Connect4System facade.
 * 
 * Thread Safety:
 * - Singleton instance is created using double-checked locking
 * - All underlying services are stateless or thread-safe
 * - Repository operations are thread-safe
 * - LockManager ensures thread-safe game operations
 */
public class Connect4SystemImpl implements Connect4System {
    
    // Singleton instance with double-checked locking
    private static volatile Connect4SystemImpl instance;
    private static final Object lock = new Object();
    
    // Services - all are stateless and thread-safe
    private final GameService gameService;
    
    /**
     * Private constructor - prevents instantiation.
     * Initializes all services and repositories.
     */
    private Connect4SystemImpl() {
        // Initialize repositories
        GameRepository repository = new InMemoryGameRepository();
        
        // Initialize services
        LockManager lockManager = new LockManager();
        WinChecker winChecker = new WinChecker();
        
        // Initialize game service
        this.gameService = new GameService(repository, lockManager, winChecker);
    }
    
    /**
     * Get singleton instance using double-checked locking pattern.
     * Thread-safe singleton implementation.
     */
    public static Connect4SystemImpl getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new Connect4SystemImpl();
                }
            }
        }
        return instance;
    }
    
    // ========== Game Management ==========
    
    @Override
    public GameId createGame() {
        return gameService.createGame();
    }
    
    @Override
    public GameId createGame(int rows, int cols) {
        return gameService.createGame(rows, cols);
    }
    
    // ========== Player Management ==========
    
    @Override
    public PlayerId joinGame(GameId gameId, String playerName, Disc disc) {
        PlayerId playerId = PlayerId.generate();
        gameService.addPlayer(gameId, playerId, playerName, disc);
        return playerId;
    }
    
    // ========== Game Operations ==========
    
    @Override
    public MoveResult makeMove(GameId gameId, PlayerId playerId, int column) {
        return gameService.makeMove(gameId, playerId, column);
    }
    
    // ========== Game Queries ==========
    
    @Override
    public GameState getGameState(GameId gameId) {
        return gameService.getGameState(gameId);
    }
    
    @Override
    public Board getBoard(GameId gameId) {
        return gameService.getBoard(gameId);
    }
    
    @Override
    public Game getGame(GameId gameId) {
        return gameService.getGame(gameId);
    }
}

