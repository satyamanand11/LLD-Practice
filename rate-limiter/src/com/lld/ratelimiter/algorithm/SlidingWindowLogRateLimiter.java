package com.lld.ratelimiter.algorithm;

import com.lld.ratelimiter.config.SlidingWindowLogParams;
import com.lld.ratelimiter.model.RateLimitResult;
import com.lld.ratelimiter.repository.RateLimitStateStore;
import com.lld.ratelimiter.state.SlidingWindowLogState;

import java.util.Deque;

public final class SlidingWindowLogRateLimiter implements RateLimiter {

    private final SlidingWindowLogParams params;
    private final RateLimitStateStore<SlidingWindowLogState> stateStore;

    public SlidingWindowLogRateLimiter(
            SlidingWindowLogParams params,
            RateLimitStateStore<SlidingWindowLogState> stateStore
    ) {
        this.params = params;
        this.stateStore = stateStore;
    }

    @Override
    public RateLimitResult allow(String clientId) {
        long now = System.currentTimeMillis();

        SlidingWindowLogState state = stateStore.getOrCreate(clientId, SlidingWindowLogState::new);

        synchronized (state) {
            Deque<Long> timestamps = state.getRequestTimestamps();

            evictOldRequests(timestamps, now);

            if (timestamps.size() < params.getMaxRequests()) {
                timestamps.addLast(now);

                int remaining =
                        params.getMaxRequests() - timestamps.size();

                return RateLimitResult.allowed(remaining);
            }

            long oldestRequestTime = timestamps.peekFirst();
            long retryAfterMs =
                    params.getWindowSizeMs() - (now - oldestRequestTime);

            return RateLimitResult.denied(retryAfterMs);
        }
    }

    private void evictOldRequests(Deque<Long> timestamps, long now) {
        long windowStart = now - params.getWindowSizeMs();

        while (!timestamps.isEmpty()
                && timestamps.peekFirst() <= windowStart) {
            timestamps.removeFirst();
        }
    }
}
