package com.lld.ratelimiter.service;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.algorithm.RateLimitingAlgorithm;
import com.lld.ratelimiter.config.ConfigurationService;
import com.lld.ratelimiter.domain.ClientEndpointKey;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;
import com.lld.ratelimiter.factory.RateLimitingAlgorithmFactory;
import com.lld.ratelimiter.locking.LockManager;
import com.lld.ratelimiter.repository.RateLimitStateRepository;

/**
 * Core service for rate limiting.
 * 
 * Responsibilities:
 * - Route requests to appropriate algorithm
 * - Manage state per (clientId, endpoint)
 * - Return structured results
 * - Ensure thread-safe operations using LockManager
 */
public class RateLimiterService {
    private final ConfigurationService configService;
    private final RateLimitingAlgorithmFactory algorithmFactory;
    private final RateLimitStateRepository stateRepository;
    private final LockManager lockManager;

    public RateLimiterService(
            ConfigurationService configService,
            RateLimitingAlgorithmFactory algorithmFactory,
            RateLimitStateRepository stateRepository,
            LockManager lockManager) {
        if (configService == null) {
            throw new IllegalArgumentException("configService cannot be null");
        }
        if (algorithmFactory == null) {
            throw new IllegalArgumentException("algorithmFactory cannot be null");
        }
        if (stateRepository == null) {
            throw new IllegalArgumentException("stateRepository cannot be null");
        }
        if (lockManager == null) {
            throw new IllegalArgumentException("lockManager cannot be null");
        }
        this.configService = configService;
        this.algorithmFactory = algorithmFactory;
        this.stateRepository = stateRepository;
        this.lockManager = lockManager;
    }

    /**
     * Checks if a request is allowed and updates state.
     * Thread-safe: Uses LockManager to ensure atomic read-modify-write operations.
     * 
     * @param clientId Client identifier
     * @param endpoint Endpoint path
     * @return RateLimitResult with allowed status, remaining count, and retryAfterMs
     */
    public RateLimitResult checkLimit(String clientId, String endpoint) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint cannot be null or empty");
        }

        ClientEndpointKey key = new ClientEndpointKey(clientId, endpoint);

        // Execute with lock to ensure atomic read-modify-write
        return lockManager.executeWithLock(key, () -> {
            // Get configuration (or default)
            EndpointConfig config = configService.getEndpointConfig(endpoint);

            // Get algorithm
            RateLimitingAlgorithm algorithm = algorithmFactory.getAlgorithm(config.getAlgorithmType());

            // Get or create state
            AlgorithmState state = stateRepository.getState(key).orElse(null);

            // Check limit using algorithm (may modify state)
            RateLimitResult result = algorithm.checkLimit(state, config);

            // Update state (algorithm may have modified it)
            if (state == null) {
                // Create initial state
                state = algorithm.createInitialState(config);
            }
            stateRepository.saveState(key, state);

            return result;
        });
    }
}

