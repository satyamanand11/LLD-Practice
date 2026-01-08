package com.lld.ratelimiter.algorithm;

import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;

/**
 * Strategy interface for rate limiting algorithms.
 * Each algorithm implements its own rate limiting logic.
 */
public interface RateLimitingAlgorithm {
    /**
     * Checks if request is allowed and updates state.
     * 
     * @param state Current algorithm state (may be null for first request)
     * @param config Endpoint configuration
     * @return RateLimitResult with allowed status, remaining count, and retryAfterMs
     */
    RateLimitResult checkLimit(AlgorithmState state, EndpointConfig config);

    /**
     * Creates initial state for a new (clientId, endpoint) pair.
     * 
     * @param config Endpoint configuration
     * @return Initial algorithm state
     */
    AlgorithmState createInitialState(EndpointConfig config);

    /**
     * Gets algorithm name/type.
     */
    String getAlgorithmName();
}

