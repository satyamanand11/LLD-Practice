package com.lld.connect4.service.facade;

import com.lld.connect4.domain.board.Board;
import com.lld.connect4.domain.game.*;

/**
 * Facade interface for Connect4 game system.
 * Provides a simplified, unified interface to the complex subsystem.
 * 
 * This facade encapsulates:
 * - Game creation and management
 * - Player management
 * - Move execution
 * - Game state queries
 */
public interface Connect4System {
    
    /**
     * Creates a new game and returns its ID.
     */
    GameId createGame();
    
    /**
     * Creates a new game with custom board dimensions.
     */
    GameId createGame(int rows, int cols);
    
    /**
     * Joins a game as a player.
     * Returns the PlayerId assigned to the player.
     */
    PlayerId joinGame(GameId gameId, String playerName, Disc disc);
    
    /**
     * Makes a move in the game.
     */
    MoveResult makeMove(GameId gameId, PlayerId playerId, int column);
    
    /**
     * Gets the current game state.
     */
    GameState getGameState(GameId gameId);
    
    /**
     * Gets the board for display.
     */
    Board getBoard(GameId gameId);
    
    /**
     * Gets full game information.
     */
    Game getGame(GameId gameId);
}

