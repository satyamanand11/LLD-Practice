package com.lld.ticketservice.locking;

import com.lld.ticketservice.domain.show.Show;
import com.lld.ticketservice.domain.show.ShowSeat;
import com.lld.ticketservice.domain.show.ShowSeatStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class InMemorySeatLockProvider implements SeatLockProvider {
    private final Map<Integer, ReentrantLock> showLocks = new ConcurrentHashMap<>();
    private final Map<Integer, Show> shows;

    public InMemorySeatLockProvider(Map<Integer, Show> shows) {
        this.shows = shows;
    }

    private ReentrantLock lockFor(int showId) {
        return showLocks.computeIfAbsent(showId, k -> new ReentrantLock(true));
    }

    public boolean tryLock(int showId, List<Integer> seats, String userId, long ttlMs) {
        ReentrantLock l = lockFor(showId);
        l.lock();
        try {
            Show show = shows.get(showId);
            long now = System.currentTimeMillis();
            for (int s : seats) {
                ShowSeat seat = show.getSeats().get(s);
                if (seat == null) return false;
                if (seat.getStatus() == ShowSeatStatus.BOOKED) return false;
                if (seat.getStatus() == ShowSeatStatus.LOCKED && seat.getLockExpiresAt() > now && !userId.equals(seat.getLockedByUser()))
                    return false;
            }
            long exp = now + ttlMs;
            for (int s : seats) {
                ShowSeat seat = show.getSeats().get(s);
                seat.setStatus(ShowSeatStatus.LOCKED);
                seat.setLockedByUser(userId);
                seat.setLockExpiresAt(exp);
            }
            return true;
        } finally {
            l.unlock();
        }
    }

    public void release(int showId, List<Integer> seats, String userId) {
        ReentrantLock l = lockFor(showId);
        l.lock();
        try {
            Show show = shows.get(showId);
            for (int s : seats) {
                ShowSeat seat = show.getSeats().get(s);
                if (seat != null && userId.equals(seat.getLockedByUser())) {
                    seat.setStatus(ShowSeatStatus.VACANT);
                    seat.setLockedByUser(null);
                    seat.setLockExpiresAt(0);
                }
            }
        } finally {
            l.unlock();
        }
    }
}
