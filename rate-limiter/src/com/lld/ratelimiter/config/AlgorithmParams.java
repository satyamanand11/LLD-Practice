package com.lld.ratelimiter.config;

public sealed interface AlgorithmParams permits TokenBucketParams, SlidingWindowLogParams {
}
