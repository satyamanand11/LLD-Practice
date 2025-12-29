package com.lld.amazon.locker.scheduler;

import java.time.Duration;

public interface Scheduler {
    void schedule(Runnable task, Duration delay);
    void shutdown();
}
