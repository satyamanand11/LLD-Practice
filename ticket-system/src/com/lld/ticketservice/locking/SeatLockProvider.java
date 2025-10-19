package com.lld.ticketservice.locking;

import java.util.List;

public interface SeatLockProvider {
    boolean tryLock(int showId, List<Integer> seats, String userId, long ttlMs);

    void release(int showId, List<Integer> seats, String userId);
}
