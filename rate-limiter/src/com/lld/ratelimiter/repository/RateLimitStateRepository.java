package com.lld.ratelimiter.repository;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.domain.ClientEndpointKey;

import java.util.Optional;

/**
 * Repository for storing rate limit state per (clientId, endpoint) pair.
 * 
 * Thread-safety:
 * - Implementation should be thread-safe
 * - State updates should be atomic
 */
public interface RateLimitStateRepository {
    /**
     * Gets the algorithm state for a (clientId, endpoint) pair.
     * Returns Optional.empty() if no state exists.
     */
    Optional<AlgorithmState> getState(ClientEndpointKey key);

    /**
     * Saves or updates the algorithm state for a (clientId, endpoint) pair.
     */
    void saveState(ClientEndpointKey key, AlgorithmState state);

    /**
     * Removes state for a (clientId, endpoint) pair.
     * Useful for cleanup or reset.
     */
    void removeState(ClientEndpointKey key);

    /**
     * Clears all state.
     * Useful for testing or reset.
     */
    void clear();
}

