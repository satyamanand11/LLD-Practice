package com.lld.ratelimiter;

import com.lld.ratelimiter.algorithm.impl.FixedWindowAlgorithm;
import com.lld.ratelimiter.algorithm.impl.SlidingWindowLogAlgorithm;
import com.lld.ratelimiter.algorithm.impl.TokenBucketAlgorithm;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;
import com.lld.ratelimiter.facade.RateLimiterFacadeImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * com.lld.ratelimiter.Main class demonstrating Rate Limiter System
 * 
 * This demonstrates:
 * - Multiple rate limiting algorithms (TokenBucket, SlidingWindowLog, FixedWindow)
 * - Per-endpoint configuration
 * - Per-client rate limiting
 * - Structured result format (allowed, remaining, retryAfterMs)
 * - Default configuration fallback
 * - Design Patterns: Strategy, Factory, Repository, Facade, Singleton
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Rate Limiter System ===\n");

        // Get singleton facade instance (Facade Pattern)
        RateLimiterFacadeImpl facade = RateLimiterFacadeImpl.getInstance();
        System.out.println("✓ Using Facade Pattern: RateLimiterFacade\n");

        // ========== CONFIGURATION SETUP ==========
        System.out.println("1. Loading Configuration at Startup");
        
        // Configure endpoints
        Map<String, EndpointConfig> configs = new HashMap<>();
        
        // Endpoint 1: TokenBucket algorithm
        Map<String, Object> tokenBucketParams = new HashMap<>();
        tokenBucketParams.put(TokenBucketAlgorithm.PARAM_CAPACITY, 10);
        tokenBucketParams.put(TokenBucketAlgorithm.PARAM_REFILL_RATE, 2.0); // 2 tokens per second
        EndpointConfig tokenBucketConfig = new EndpointConfig(
                "/api/users",
                TokenBucketAlgorithm.ALGORITHM_NAME,
                tokenBucketParams
        );
        configs.put("/api/users", tokenBucketConfig);
        System.out.println("   Configured /api/users: TokenBucket (capacity=10, refillRate=2/sec)");
        
        // Endpoint 2: SlidingWindowLog algorithm
        Map<String, Object> slidingWindowParams = new HashMap<>();
        slidingWindowParams.put(SlidingWindowLogAlgorithm.PARAM_WINDOW_SIZE_MS, 60000L); // 60 seconds
        slidingWindowParams.put(SlidingWindowLogAlgorithm.PARAM_MAX_REQUESTS, 5);
        EndpointConfig slidingWindowConfig = new EndpointConfig(
                "/api/orders",
                SlidingWindowLogAlgorithm.ALGORITHM_NAME,
                slidingWindowParams
        );
        configs.put("/api/orders", slidingWindowConfig);
        System.out.println("   Configured /api/orders: SlidingWindowLog (window=60s, maxRequests=5)");
        
        // Endpoint 3: FixedWindow algorithm
        Map<String, Object> fixedWindowParams = new HashMap<>();
        fixedWindowParams.put(FixedWindowAlgorithm.PARAM_WINDOW_SIZE_MS, 10000L); // 10 seconds
        fixedWindowParams.put(FixedWindowAlgorithm.PARAM_MAX_REQUESTS, 3);
        EndpointConfig fixedWindowConfig = new EndpointConfig(
                "/api/payments",
                FixedWindowAlgorithm.ALGORITHM_NAME,
                fixedWindowParams
        );
        configs.put("/api/payments", fixedWindowConfig);
        System.out.println("   Configured /api/payments: FixedWindow (window=10s, maxRequests=3)");
        
        // Default configuration
        Map<String, Object> defaultParams = new HashMap<>();
        defaultParams.put(TokenBucketAlgorithm.PARAM_CAPACITY, 50);
        defaultParams.put(TokenBucketAlgorithm.PARAM_REFILL_RATE, 5.0);
        EndpointConfig defaultConfig = new EndpointConfig(
                "default",
                TokenBucketAlgorithm.ALGORITHM_NAME,
                defaultParams
        );
        System.out.println("   Default config: TokenBucket (capacity=50, refillRate=5/sec)\n");
        
        // Load configuration
        facade.loadConfiguration(configs, defaultConfig);
        System.out.println("   ✓ Configuration loaded\n");

        // ========== DEMONSTRATION ==========

        System.out.println("2. Testing TokenBucket Algorithm (/api/users)");
        System.out.println("   Client: client1, Endpoint: /api/users");
        System.out.println("   Making 12 requests (capacity=10, refillRate=2/sec)...\n");
        
        for (int i = 1; i <= 12; i++) {
            RateLimitResult result = facade.checkLimit("client1", "/api/users");
            System.out.println(String.format("   Request %2d: allowed=%5s, remaining=%2d, retryAfterMs=%s",
                    i,
                    result.isAllowed(),
                    result.getRemaining(),
                    result.getRetryAfterMs() != null ? result.getRetryAfterMs() + "ms" : "null"));
            
            if (!result.isAllowed() && i < 12) {
                System.out.println("   → Rate limit exceeded! Waiting " + result.getRetryAfterMs() + "ms...");
                try {
                    Thread.sleep(result.getRetryAfterMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // Small delay to see refill in action
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println();

        System.out.println("3. Testing SlidingWindowLog Algorithm (/api/orders)");
        System.out.println("   Client: client2, Endpoint: /api/orders");
        System.out.println("   Making 7 requests (maxRequests=5 in 60s window)...\n");
        
        for (int i = 1; i <= 7; i++) {
            RateLimitResult result = facade.checkLimit("client2", "/api/orders");
            System.out.println(String.format("   Request %d: allowed=%5s, remaining=%2d, retryAfterMs=%s",
                    i,
                    result.isAllowed(),
                    result.getRemaining(),
                    result.getRetryAfterMs() != null ? result.getRetryAfterMs() + "ms" : "null"));
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();

        System.out.println("4. Testing FixedWindow Algorithm (/api/payments)");
        System.out.println("   Client: client3, Endpoint: /api/payments");
        System.out.println("   Making 5 requests (maxRequests=3 in 10s window)...\n");
        
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = facade.checkLimit("client3", "/api/payments");
            System.out.println(String.format("   Request %d: allowed=%5s, remaining=%2d, retryAfterMs=%s",
                    i,
                    result.isAllowed(),
                    result.getRemaining(),
                    result.getRetryAfterMs() != null ? result.getRetryAfterMs() + "ms" : "null"));
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();

        System.out.println("5. Testing Default Configuration");
        System.out.println("   Client: client4, Endpoint: /api/unknown (not configured)");
        System.out.println("   Should use default TokenBucket config...\n");
        
        for (int i = 1; i <= 3; i++) {
            RateLimitResult result = facade.checkLimit("client4", "/api/unknown");
            System.out.println(String.format("   Request %d: allowed=%5s, remaining=%2d, retryAfterMs=%s",
                    i,
                    result.isAllowed(),
                    result.getRemaining(),
                    result.getRetryAfterMs() != null ? result.getRetryAfterMs() + "ms" : "null"));
        }
        System.out.println();

        System.out.println("6. Testing Per-Client Isolation");
        System.out.println("   Same endpoint, different clients should have independent limits...\n");
        
        RateLimitResult result1 = facade.checkLimit("clientA", "/api/users");
        RateLimitResult result2 = facade.checkLimit("clientB", "/api/users");
        RateLimitResult result3 = facade.checkLimit("clientA", "/api/users");
        
        System.out.println("   clientA, request 1: allowed=" + result1.isAllowed() + ", remaining=" + result1.getRemaining());
        System.out.println("   clientB, request 1: allowed=" + result2.isAllowed() + ", remaining=" + result2.getRemaining());
        System.out.println("   clientA, request 2: allowed=" + result3.isAllowed() + ", remaining=" + result3.getRemaining());
        System.out.println("   ✓ Each client has independent rate limit state\n");

        System.out.println("=== Demo Complete ===");
        System.out.println("\nKey Features Demonstrated:");
        System.out.println("✓ Configuration loaded at startup");
        System.out.println("✓ Multiple rate limiting algorithms (TokenBucket, SlidingWindowLog, FixedWindow)");
        System.out.println("✓ Per-endpoint configuration");
        System.out.println("✓ Per-client rate limiting");
        System.out.println("✓ Structured result format (allowed, remaining, retryAfterMs)");
        System.out.println("✓ Default configuration fallback");
        System.out.println("\nDesign Patterns Used:");
        System.out.println("✓ Strategy Pattern: RateLimitingAlgorithm interface with multiple implementations");
        System.out.println("✓ Factory Pattern: RateLimitingAlgorithmFactory for algorithm creation");
        System.out.println("✓ Repository Pattern: RateLimitStateRepository for state storage");
        System.out.println("✓ Facade Pattern: Unified interface through RateLimiterFacade");
        System.out.println("✓ Singleton Pattern: Thread-safe facade instance");
        System.out.println("\nArchitecture Highlights:");
        System.out.println("✓ ClientEndpointKey as value object for composite keys");
        System.out.println("✓ Algorithm-specific state (TokenBucketState, SlidingWindowLogState, etc.)");
        System.out.println("✓ Thread-safe state repository (ConcurrentHashMap)");
        System.out.println("✓ Immutable configuration");
        System.out.println("✓ Extensible algorithm system");
    }
}

