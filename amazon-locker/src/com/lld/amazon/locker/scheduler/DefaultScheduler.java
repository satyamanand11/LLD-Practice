package com.lld.amazon.locker.scheduler;

import java.time.Duration;
import java.util.concurrent.*;

public class DefaultScheduler implements Scheduler {

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("locker-expiry-scheduler");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void schedule(Runnable task, Duration delay) {
        exec.schedule(task, Math.max(0, delay.toMillis()), TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        exec.shutdownNow();
    }
}
