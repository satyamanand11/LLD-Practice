package com.lld.ratelimiter.algorithm;

/**
 * Interface for algorithm-specific state.
 * Each algorithm implementation provides its own state representation.
 * State is stored per (clientId, endpoint) pair.
 */
public interface AlgorithmState {
    /**
     * Gets the algorithm type this state belongs to.
     */
    String getAlgorithmType();
}

