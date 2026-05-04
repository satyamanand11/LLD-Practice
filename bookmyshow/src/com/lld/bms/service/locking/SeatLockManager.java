package com.lld.bms.service.locking;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class SeatLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeWithLocks(List<String> showSeatIds, Supplier<T> operation) {
        List<String> sortedSeatIds = new ArrayList<>(new TreeSet<>(showSeatIds));
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (String showSeatId : sortedSeatIds) {
                ReentrantLock lock = locks.computeIfAbsent(showSeatId, id -> new ReentrantLock());
                lock.lock();
                acquiredLocks.add(lock);
            }
            return operation.get();
        } finally {
            for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                acquiredLocks.get(i).unlock();
            }
        }
    }
}
