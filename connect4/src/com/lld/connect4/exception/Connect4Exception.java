package com.lld.connect4.exception;

/**
 * Base exception for all Connect4 game exceptions.
 * Follows exception hierarchy best practices.
 */
public class Connect4Exception extends RuntimeException {
    public Connect4Exception(String message) {
        super(message);
    }

    public Connect4Exception(String message, Throwable cause) {
        super(message, cause);
    }
}

