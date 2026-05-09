package com.lld.ratelimiter.algorithm;

import com.lld.ratelimiter.config.TokenBucketParams;
import com.lld.ratelimiter.model.RateLimitResult;
import com.lld.ratelimiter.repository.RateLimitStateStore;
import com.lld.ratelimiter.state.TokenBucketState;

public final class TokenBucketRateLimiter implements RateLimiter {

    private final TokenBucketParams params;
    private final RateLimitStateStore<TokenBucketState> stateStore;

    public TokenBucketRateLimiter(
            TokenBucketParams params,
            RateLimitStateStore<TokenBucketState> stateStore
    ) {
        this.params = params;
        this.stateStore = stateStore;
    }

    @Override
    public RateLimitResult allow(String clientId) {
        long now = System.currentTimeMillis();

        TokenBucketState state = stateStore.getOrCreate(
                clientId,
                () -> new TokenBucketState(params.getCapacity(), now)
        );

        synchronized (state) {
            refill(state, now);

            if (state.getAvailableTokens() >= 1.0) {
                state.setAvailableTokens(state.getAvailableTokens() - 1.0);

                return RateLimitResult.allowed(
                        (int) Math.floor(state.getAvailableTokens())
                );
            }

            long retryAfterMs = computeRetryAfterMs(state);
            return RateLimitResult.denied(retryAfterMs);
        }
    }

    private void refill(TokenBucketState state, long now) {
        long elapsedMs = now - state.getLastRefillTimestampMs();

        if (elapsedMs <= 0) {
            return;
        }

        double tokensToAdd =
                (elapsedMs / 1000.0) * params.getRefillRatePerSecond();

        double updatedTokens = Math.min(
                params.getCapacity(),
                state.getAvailableTokens() + tokensToAdd
        );

        state.setAvailableTokens(updatedTokens);
        state.setLastRefillTimestampMs(now);
    }

    private long computeRetryAfterMs(TokenBucketState state) {
        double missingTokens = 1.0 - state.getAvailableTokens();
        double secondsToWait =
                missingTokens / params.getRefillRatePerSecond();

        return (long) Math.ceil(secondsToWait * 1000);
    }
}
