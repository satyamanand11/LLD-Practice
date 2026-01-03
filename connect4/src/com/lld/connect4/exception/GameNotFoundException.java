package com.lld.connect4.exception;

/**
 * Exception thrown when a requested game is not found.
 */
public class GameNotFoundException extends Connect4Exception {
    public GameNotFoundException(String message) {
        super(message);
    }
}

