package com.lld.ratelimiter.repository;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.domain.ClientEndpointKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of RateLimitStateRepository.
 * 
 * Thread-safety:
 * - Uses ConcurrentHashMap for thread-safe storage
 * - State updates are atomic at map level
 * - Algorithm-specific atomicity is handled by algorithms
 */
public class InMemoryRateLimitStateRepository implements RateLimitStateRepository {
    private final Map<ClientEndpointKey, AlgorithmState> store = new ConcurrentHashMap<>();

    @Override
    public Optional<AlgorithmState> getState(ClientEndpointKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public void saveState(ClientEndpointKey key, AlgorithmState state) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }
        store.put(key, state);
    }

    @Override
    public void removeState(ClientEndpointKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        store.remove(key);
    }

    @Override
    public void clear() {
        store.clear();
    }
}

