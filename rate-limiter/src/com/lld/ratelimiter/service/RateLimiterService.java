package com.lld.ratelimiter.service;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.algorithm.RateLimitingAlgorithm;
import com.lld.ratelimiter.config.ConfigurationService;
import com.lld.ratelimiter.domain.ClientEndpointKey;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;
import com.lld.ratelimiter.factory.RateLimitingAlgorithmFactory;
import com.lld.ratelimiter.repository.RateLimitStateRepository;

/**
 * Core service for rate limiting.
 * 
 * Responsibilities:
 * - Route requests to appropriate algorithm
 * - Manage state per (clientId, endpoint)
 * - Return structured results
 */
public class RateLimiterService {
    private final ConfigurationService configService;
    private final RateLimitingAlgorithmFactory algorithmFactory;
    private final RateLimitStateRepository stateRepository;

    public RateLimiterService(
            ConfigurationService configService,
            RateLimitingAlgorithmFactory algorithmFactory,
            RateLimitStateRepository stateRepository) {
        if (configService == null) {
            throw new IllegalArgumentException("configService cannot be null");
        }
        if (algorithmFactory == null) {
            throw new IllegalArgumentException("algorithmFactory cannot be null");
        }
        if (stateRepository == null) {
            throw new IllegalArgumentException("stateRepository cannot be null");
        }
        this.configService = configService;
        this.algorithmFactory = algorithmFactory;
        this.stateRepository = stateRepository;
    }

    /**
     * Checks if a request is allowed and updates state.
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

        // Get configuration (or default)
        EndpointConfig config = configService.getEndpointConfig(endpoint);

        // Get algorithm
        RateLimitingAlgorithm algorithm = algorithmFactory.getAlgorithm(config.getAlgorithmType());

        // Get or create state
        ClientEndpointKey key = new ClientEndpointKey(clientId, endpoint);
        AlgorithmState state = stateRepository.getState(key).orElse(null);

        // Check limit using algorithm
        RateLimitResult result = algorithm.checkLimit(state, config);

        // Update state (algorithm may have modified it)
        if (state == null) {
            // Create initial state
            state = algorithm.createInitialState(config);
        }
        stateRepository.saveState(key, state);

        return result;
    }
}

