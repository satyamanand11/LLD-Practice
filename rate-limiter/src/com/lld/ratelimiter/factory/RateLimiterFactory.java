package com.lld.ratelimiter.factory;

import com.lld.ratelimiter.algorithm.RateLimiter;
import com.lld.ratelimiter.algorithm.SlidingWindowLogRateLimiter;
import com.lld.ratelimiter.algorithm.TokenBucketRateLimiter;
import com.lld.ratelimiter.config.AlgorithmParams;
import com.lld.ratelimiter.config.SlidingWindowLogParams;
import com.lld.ratelimiter.config.TokenBucketParams;
import com.lld.ratelimiter.model.RateLimitAlgorithmType;
import com.lld.ratelimiter.repository.RateLimitStateStore;
import com.lld.ratelimiter.repository.InMemoryRateLimitStateStore;
import com.lld.ratelimiter.state.SlidingWindowLogState;
import com.lld.ratelimiter.state.TokenBucketState;

public final class RateLimiterFactory {

    public RateLimiter create(
            RateLimitAlgorithmType algorithmType,
            AlgorithmParams params
    ) {
        return switch (algorithmType) {
            case TOKEN_BUCKET -> {
                TokenBucketParams tokenBucketParams = (TokenBucketParams) params;

                RateLimitStateStore<TokenBucketState> stateStore =
                        new InMemoryRateLimitStateStore<>();

                yield new TokenBucketRateLimiter(tokenBucketParams, stateStore);
            }

            case SLIDING_WINDOW_LOG -> {
                SlidingWindowLogParams slidingWindowLogParams =
                        (SlidingWindowLogParams) params;

                RateLimitStateStore<SlidingWindowLogState> stateStore =
                        new InMemoryRateLimitStateStore<>();

                yield new SlidingWindowLogRateLimiter(slidingWindowLogParams, stateStore);
            }
        };
    }
}