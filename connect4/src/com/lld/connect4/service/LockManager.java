package com.lld.connect4.service;

import com.lld.connect4.domain.game.GameId;
import com.lld.connect4.exception.Connect4Exception;
import com.lld.connect4.exception.ConcurrencyException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages locks for games to prevent concurrent modification.
 * Follows Single Responsibility Principle and provides thread-safety.
 */
public class LockManager {
    private final Map<GameId, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Executes an operation with a lock on the specified game.
     * Ensures thread-safety for game operations.
     * Preserves Connect4Exception types, only wraps unexpected exceptions.
     */
    public <T> T executeWithLock(GameId gameId, LockedOperation<T> operation) {
        ReentrantLock lock = locks.computeIfAbsent(gameId, k -> new ReentrantLock());
        
        try {
            if (!lock.tryLock()) {
                throw new ConcurrencyException("Game " + gameId + " is currently being modified by another operation");
            }
            
            try {
                return operation.execute();
            } finally {
                lock.unlock();
            }
        } catch (Connect4Exception e) {
            // Preserve Connect4Exception types
            throw e;
        } catch (Exception e) {
            // Wrap unexpected exceptions
            throw new RuntimeException("Error executing locked operation", e);
        }
    }

    /**
     * Removes the lock for a game (cleanup when game is finished).
     */
    public void releaseLock(GameId gameId) {
        locks.remove(gameId);
    }

    /**
     * Functional interface for operations that need to be executed with a lock.
     */
    @FunctionalInterface
    public interface LockedOperation<T> {
        T execute();
    }
}

