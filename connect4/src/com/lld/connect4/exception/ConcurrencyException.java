package com.lld.connect4.exception;

/**
 * Exception thrown when concurrent access conflicts occur.
 */
public class ConcurrencyException extends Connect4Exception {
    public ConcurrencyException(String message) {
        super(message);
    }
}

