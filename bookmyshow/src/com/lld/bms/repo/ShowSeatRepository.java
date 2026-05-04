package com.lld.bms.repo;

import com.lld.bms.domain.ShowSeat;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowSeatRepository extends Repository<ShowSeat, String> {

    List<ShowSeat> findByIds(List<String> ids);

    List<ShowSeat> findByShowId(String showId);

    /**
     * Find seats whose lock has expired (used by the lock-expiry sweeper).
     */
    List<ShowSeat> findExpiredLocks(LocalDateTime now);
}
