package com.lld.cricbuzz.locking;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Centralized Lock Manager for thread-safe operations
 * 
 * In real-world systems, locking is typically managed at the service layer
 * rather than in repositories. This provides:
 * - Centralized lock management
 * - Consistent locking strategy
 * - Better separation of concerns
 * - Easier to monitor and debug
 * 
 * Repositories remain simple data access layers
 */
public class LockManager {
    private static final LockManager instance = new LockManager();
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static final int DEFAULT_LOCK_TIMEOUT_SECONDS = 5;
    
    private LockManager() {
        // Singleton
    }
    
    public static LockManager getInstance() {
        return instance;
    }
    
    /**
     * Get or create a lock for a given entity
     * 
     * @param entityType Type of entity (e.g., "Match", "Player")
     * @param entityId Unique identifier for the entity
     * @return ReentrantLock for the entity
     */
    public ReentrantLock getLock(String entityType, String entityId) {
        String lockKey = entityType + ":" + entityId;
        return locks.computeIfAbsent(lockKey, k -> new ReentrantLock(false));
    }
    
    /**
     * Execute an operation with entity-level lock
     * 
     * @param entityType Type of entity
     * @param entityId Entity identifier
     * @param operation The operation to execute
     * @param <T> Return type
     * @return Result of the operation
     * @throws IllegalStateException if lock cannot be acquired
     */
    public <T> T executeWithLock(String entityType, String entityId, 
                                 Supplier<T> operation) {
        ReentrantLock lock = getLock(entityType, entityId);
        
        try {
            if (lock.tryLock(DEFAULT_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    return operation.get();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException(
                    String.format("Could not acquire lock for %s:%s within %d seconds. " +
                                "Possible deadlock or high contention.",
                                entityType, entityId, DEFAULT_LOCK_TIMEOUT_SECONDS));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                String.format("Lock acquisition interrupted for %s:%s", entityType, entityId), e);
        }
    }
    
    /**
     * Execute an operation with entity-level lock (void return)
     */
    public void executeWithLock(String entityType, String entityId, Runnable operation) {
        executeWithLock(entityType, entityId, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * Execute with custom timeout
     */
    public <T> T executeWithLock(String entityType, String entityId, 
                                int timeoutSeconds, Supplier<T> operation) {
        ReentrantLock lock = getLock(entityType, entityId);
        
        try {
            if (lock.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                try {
                    return operation.get();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException(
                    String.format("Could not acquire lock for %s:%s within %d seconds",
                                entityType, entityId, timeoutSeconds));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                String.format("Lock acquisition interrupted for %s:%s", entityType, entityId), e);
        }
    }
    
    /**
     * Check if a lock is currently held
     */
    public boolean isLocked(String entityType, String entityId) {
        ReentrantLock lock = locks.get(entityType + ":" + entityId);
        return lock != null && lock.isLocked();
    }
    
    /**
     * Get lock statistics (for monitoring)
     */
    public int getActiveLockCount() {
        return (int) locks.values().stream()
            .filter(ReentrantLock::isLocked)
            .count();
    }
    
    /**
     * Clear locks for a specific entity (cleanup)
     */
    public void clearLock(String entityType, String entityId) {
        locks.remove(entityType + ":" + entityId);
    }
}

