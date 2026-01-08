package com.lld.ratelimiter.facade;

import com.lld.ratelimiter.config.ConfigurationService;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;
import com.lld.ratelimiter.factory.RateLimitingAlgorithmFactory;
import com.lld.ratelimiter.locking.LockManager;
import com.lld.ratelimiter.repository.InMemoryRateLimitStateRepository;
import com.lld.ratelimiter.repository.RateLimitStateRepository;
import com.lld.ratelimiter.service.RateLimiterService;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe Singleton implementation of RateLimiterFacade.
 * 
 * Wires:
 * - ConfigurationService
 * - RateLimitingAlgorithmFactory
 * - RateLimitStateRepository
 * - RateLimiterService
 * 
 * Note:
 * - In real systems this wiring is done via DI (Spring)
 * - Singleton is acceptable and expected in LLD interviews
 */
public class RateLimiterFacadeImpl implements RateLimiterFacade {
    private static volatile RateLimiterFacadeImpl instance;
    private static final Object lock = new Object();

    private final RateLimiterService rateLimiterService;
    private final ConfigurationService configService;

    private RateLimiterFacadeImpl() {
        // Initialize dependencies
        this.configService = new ConfigurationService();
        RateLimitingAlgorithmFactory algorithmFactory = new RateLimitingAlgorithmFactory();
        RateLimitStateRepository stateRepository = new InMemoryRateLimitStateRepository();
        LockManager lockManager = LockManager.getInstance();

        // Wire service
        this.rateLimiterService = new RateLimiterService(
                configService,
                algorithmFactory,
                stateRepository,
                lockManager
        );

        // Load default configuration
        loadDefaultConfiguration();
    }

    /**
     * Get singleton instance using double-checked locking pattern.
     * Thread-safe singleton implementation.
     */
    public static RateLimiterFacadeImpl getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new RateLimiterFacadeImpl();
                }
            }
        }
        return instance;
    }

    /**
     * Loads configuration at startup.
     * In a real system, this would load from file/database.
     */
    public void loadConfiguration(Map<String, EndpointConfig> configs, EndpointConfig defaultConfig) {
        configService.loadConfiguration(configs, defaultConfig);
    }

    @Override
    public RateLimitResult checkLimit(String clientId, String endpoint) {
        return rateLimiterService.checkLimit(clientId, endpoint);
    }

    /**
     * Loads default configuration for demo purposes.
     * In production, this would be loaded from configuration file/database.
     */
    private void loadDefaultConfiguration() {
        Map<String, Object> defaultParams = new HashMap<>();
        defaultParams.put("capacity", 50);
        defaultParams.put("refillRatePerSecond", 5.0);
        EndpointConfig defaultConfig = new EndpointConfig(
                "default",
                "TokenBucket",
                defaultParams
        );

        Map<String, EndpointConfig> configs = new HashMap<>();
        configService.loadConfiguration(configs, defaultConfig);
    }
}

