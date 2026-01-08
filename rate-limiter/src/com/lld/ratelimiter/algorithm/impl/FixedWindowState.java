package com.lld.ratelimiter.algorithm.impl;

import com.lld.ratelimiter.algorithm.AlgorithmState;

/**
 * State for FixedWindow algorithm.
 */
public class FixedWindowState implements AlgorithmState {
    private int count;
    private long windowStartTime;

    public FixedWindowState(int count, long windowStartTime) {
        this.count = count;
        this.windowStartTime = windowStartTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getWindowStartTime() {
        return windowStartTime;
    }

    public void setWindowStartTime(long windowStartTime) {
        this.windowStartTime = windowStartTime;
    }

    @Override
    public String getAlgorithmType() {
        return "FixedWindow";
    }

    @Override
    public String toString() {
        return "FixedWindowState{" +
                "count=" + count +
                ", windowStartTime=" + windowStartTime +
                '}';
    }
}

