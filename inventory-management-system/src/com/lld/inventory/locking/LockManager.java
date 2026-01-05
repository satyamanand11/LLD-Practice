package com.lld.inventory.locking;

import com.lld.inventory.domain.InventoryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Centralized lock registry for entity-level locks.
 * Used only at the service layer.
 * 
 * Key points:
 * - Lock key = InventoryKey
 * - Uses ReentrantLock
 * - Singleton is acceptable for LLD (DI-managed in real systems)
 * 
 * Important rule:
 * - Single-entity operations → one lock
 * - Multi-entity operations → acquire locks in sorted order of keys to prevent deadlocks
 * 
 * LockManager must NOT:
 * - Hide multi-lock orchestration
 * - Implicitly manage transactions
 */
public class LockManager {
    private static final LockManager instance = new LockManager();
    private final ConcurrentHashMap<InventoryKey, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static final int DEFAULT_LOCK_TIMEOUT_SECONDS = 5;

    private LockManager() {
        // Singleton
    }

    public static LockManager getInstance() {
        return instance;
    }

    /**
     * Gets or creates a lock for the given InventoryKey.
     */
    public ReentrantLock getLock(InventoryKey key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock(false));
    }

    /**
     * Executes an operation with a lock on a single InventoryKey.
     * Handles lock acquisition, timeout, and thread interruption.
     */
    public <T> T executeWithLock(InventoryKey key, Supplier<T> operation) {
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
     * Executes an operation with locks on multiple InventoryKeys.
     * Locks are acquired in sorted order to prevent deadlocks.
     * All locks are acquired before executing the operation.
     */
    public <T> T executeWithLocks(List<InventoryKey> keys, Supplier<T> operation) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        if (keys.size() == 1) {
            return executeWithLock(keys.get(0), operation);
        }

        // Sort keys to ensure deterministic lock ordering (deadlock prevention)
        List<InventoryKey> sortedKeys = new ArrayList<>(keys);
        sortedKeys.sort(InventoryKey::compareTo);

        // Acquire all locks in sorted order
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        boolean allAcquired = false;
        try {
            for (InventoryKey key : sortedKeys) {
                ReentrantLock lock = getLock(key);
                boolean acquired = lock.tryLock(DEFAULT_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!acquired) {
                    // Release all previously acquired locks
                    for (ReentrantLock acquiredLock : acquiredLocks) {
                        acquiredLock.unlock();
                    }
                    throw new IllegalStateException("Could not acquire all locks within timeout");
                }
                acquiredLocks.add(lock);
            }
            allAcquired = true;
            return operation.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt flag
            // Release all acquired locks
            for (ReentrantLock acquiredLock : acquiredLocks) {
                acquiredLock.unlock();
            }
            throw new RuntimeException("Interrupted while acquiring locks", e);
        } finally {
            if (allAcquired) {
                // Release all locks in reverse order (good practice, though not strictly necessary)
                for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                    acquiredLocks.get(i).unlock();
                }
            }
        }
    }

    /**
     * Executes a void operation with a lock on a single InventoryKey.
     */
    public void executeWithLock(InventoryKey key, Runnable operation) {
        executeWithLock(key, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * Executes a void operation with locks on multiple InventoryKeys.
     */
    public void executeWithLocks(List<InventoryKey> keys, Runnable operation) {
        executeWithLocks(keys, () -> {
            operation.run();
            return null;
        });
    }
}

