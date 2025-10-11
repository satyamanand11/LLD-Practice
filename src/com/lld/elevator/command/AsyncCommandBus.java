package com.lld.elevator.command;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncCommandBus implements CommandBus {
    private final BlockingQueue<Command> q;
    private final ExecutorService pool;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AsyncCommandBus(int capacity, int workers) {
        q = new ArrayBlockingQueue<>(capacity);
        pool = Executors.newFixedThreadPool(workers);
        for (int i = 0; i < workers; i++) {
            pool.submit(() -> {
                try {
                    while (running.get()) {
                        Command c = q.poll(100, TimeUnit.MILLISECONDS);
                        if (c != null) c.execute();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void submit(Command c) { q.offer(c); }
    public void shutdown() { running.set(false); pool.shutdownNow(); }
}
