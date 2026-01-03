package com.lld.connect4.exception;

/**
 * Exception thrown when an invalid move is attempted.
 */
public class InvalidMoveException extends Connect4Exception {
    public InvalidMoveException(String message) {
        super(message);
    }
}

