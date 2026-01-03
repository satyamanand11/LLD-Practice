package com.lld.connect4.service;

import com.lld.connect4.domain.board.Board;
import com.lld.connect4.domain.game.*;
import com.lld.connect4.domain.rules.WinChecker;
import com.lld.connect4.exception.GameNotFoundException;
import com.lld.connect4.exception.InvalidMoveException;
import com.lld.connect4.repo.GameRepository;

/**
 * Service layer for game operations.
 * Follows Service pattern and Single Responsibility Principle.
 * Handles business logic and coordinates between domain and repository.
 */
public class GameService {
    private final GameRepository gameRepository;
    private final LockManager lockManager;
    private final WinChecker winChecker;

    public GameService(GameRepository gameRepository, LockManager lockManager, WinChecker winChecker) {
        if (gameRepository == null) {
            throw new IllegalArgumentException("GameRepository cannot be null");
        }
        if (lockManager == null) {
            throw new IllegalArgumentException("LockManager cannot be null");
        }
        if (winChecker == null) {
            throw new IllegalArgumentException("WinChecker cannot be null");
        }
        this.gameRepository = gameRepository;
        this.lockManager = lockManager;
        this.winChecker = winChecker;
    }

    /**
     * Creates a new game.
     */
    public GameId createGame() {
        GameId gameId = GameId.generate();
        Board board = new Board();
        Game game = new Game(gameId, board, winChecker);
        gameRepository.save(game);
        return gameId;
    }

    /**
     * Creates a new game with custom board dimensions.
     */
    public GameId createGame(int rows, int cols) {
        GameId gameId = GameId.generate();
        Board board = new Board(rows, cols);
        Game game = new Game(gameId, board, winChecker);
        gameRepository.save(game);
        return gameId;
    }

    /**
     * Adds a player to the game.
     */
    public void addPlayer(GameId gameId, PlayerId playerId, String playerName, Disc disc) {
        lockManager.executeWithLock(gameId, () -> {
            Game game = getGame(gameId);
            Player player = new Player(playerId, playerName, disc);
            
            if (game.getPlayer1() == null) {
                game.addPlayer1(player);
            } else if (game.getPlayer2() == null) {
                game.addPlayer2(player);
            } else {
                throw new IllegalStateException("Game already has two players");
            }
            
            gameRepository.save(game);
            return null;
        });
    }

    /**
     * Makes a move in the game.
     */
    public MoveResult makeMove(GameId gameId, PlayerId playerId, int column) {
        return lockManager.executeWithLock(gameId, () -> {
            Game game = getGame(gameId);
            
            try {
                MoveResult result = game.makeMove(playerId, column);
                
                if (!result.isValid()) {
                    throw new InvalidMoveException("Invalid move: Column " + column + " is full or out of bounds");
                }
                
                gameRepository.save(game);
                return result;
            } catch (IllegalStateException e) {
                // Convert domain IllegalStateException to InvalidMoveException
                throw new InvalidMoveException(e.getMessage());
            }
        });
    }

    /**
     * Gets game information.
     */
    public Game getGame(GameId gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + gameId));
    }

    /**
     * Gets the current state of the game.
     */
    public GameState getGameState(GameId gameId) {
        Game game = getGame(gameId);
        return game.getState();
    }

    /**
     * Gets the board state for display.
     */
    public Board getBoard(GameId gameId) {
        Game game = getGame(gameId);
        return game.getBoard();
    }
}

