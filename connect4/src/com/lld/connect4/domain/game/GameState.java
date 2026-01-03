package com.lld.connect4.domain.game;

/**
 * Enum representing the possible states of a Connect4 game.
 * Follows State pattern principles.
 */
public enum GameState {
    WAITING_FOR_PLAYERS,  // Game created but waiting for second player
    IN_PROGRESS,          // Game is active
    FINISHED_WIN,         // Game finished with a winner
    FINISHED_DRAW,        // Game finished in a draw
    ABANDONED             // Game was abandoned
}

