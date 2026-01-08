package com.lld.ratelimiter.factory;

import com.lld.ratelimiter.algorithm.RateLimitingAlgorithm;
import com.lld.ratelimiter.algorithm.impl.FixedWindowAlgorithm;
import com.lld.ratelimiter.algorithm.impl.SlidingWindowLogAlgorithm;
import com.lld.ratelimiter.algorithm.impl.TokenBucketAlgorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating rate limiting algorithm instances.
 * Centralizes algorithm creation logic.
 */
public class RateLimitingAlgorithmFactory {
    private final Map<String, RateLimitingAlgorithm> algorithmCache = new HashMap<>();

    public RateLimitingAlgorithmFactory() {
        // Pre-register known algorithms
        registerAlgorithm(new TokenBucketAlgorithm());
        registerAlgorithm(new SlidingWindowLogAlgorithm());
        registerAlgorithm(new FixedWindowAlgorithm());
    }

    /**
     * Registers an algorithm instance.
     */
    public void registerAlgorithm(RateLimitingAlgorithm algorithm) {
        algorithmCache.put(algorithm.getAlgorithmName(), algorithm);
    }

    /**
     * Creates or retrieves an algorithm instance by name.
     * 
     * @param algorithmType Algorithm type name (e.g., "TokenBucket", "SlidingWindowLog")
     * @return Algorithm instance
     * @throws IllegalArgumentException if algorithm type is not supported
     */
    public RateLimitingAlgorithm getAlgorithm(String algorithmType) {
        if (algorithmType == null || algorithmType.isBlank()) {
            throw new IllegalArgumentException("algorithmType cannot be null or empty");
        }

        RateLimitingAlgorithm algorithm = algorithmCache.get(algorithmType);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unsupported algorithm type: " + algorithmType);
        }
        return algorithm;
    }

    /**
     * Gets all registered algorithm types.
     */
    public java.util.Set<String> getSupportedAlgorithms() {
        return new java.util.HashSet<>(algorithmCache.keySet());
    }
}

