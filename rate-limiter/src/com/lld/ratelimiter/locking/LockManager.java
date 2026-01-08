package com.lld.ratelimiter.locking;

import com.lld.ratelimiter.domain.ClientEndpointKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Centralized lock registry for entity-level locks.
 * Used at the service layer to ensure thread-safe operations.
 * 
 * Key points:
 * - Lock key = ClientEndpointKey
 * - Uses ReentrantLock
 * - Singleton is acceptable for LLD (DI-managed in real systems)
 * 
 * Important rule:
 * - Single-entity operations → one lock
 * - Multi-entity operations → acquire locks in sorted order of keys to prevent deadlocks
 */
public class LockManager {
    private static final LockManager instance = new LockManager();
    private final ConcurrentHashMap<ClientEndpointKey, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static final int DEFAULT_LOCK_TIMEOUT_SECONDS = 5;

    private LockManager() {
        // Singleton
    }

    public static LockManager getInstance() {
        return instance;
    }

    /**
     * Gets or creates a lock for the given ClientEndpointKey.
     */
    public ReentrantLock getLock(ClientEndpointKey key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock(false));
    }

    /**
     * Executes an operation with a lock on a single ClientEndpointKey.
     * Handles lock acquisition, timeout, and thread interruption.
     */
    public <T> T executeWithLock(ClientEndpointKey key, Supplier<T> operation) {
        ReentrantLock lock = getLock(key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(DEFAULT_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Could not acquire lock for " + key + " within timeout");
            }
            return operation.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt flag
            throw new RuntimeException("Interrupted while acquiring lock for " + key, e);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * Executes a void operation with a lock on a single ClientEndpointKey.
     */
    public void executeWithLock(ClientEndpointKey key, Runnable operation) {
        executeWithLock(key, () -> {
            operation.run();
            return null;
        });
    }
}

