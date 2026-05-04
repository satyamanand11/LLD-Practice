package com.lld.bms.repo;

import com.lld.bms.domain.SeatStatus;
import com.lld.bms.domain.ShowSeat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryShowSeatRepository implements ShowSeatRepository {
    private final ConcurrentHashMap<String, ShowSeat> store = new ConcurrentHashMap<>();

    @Override
    public void save(ShowSeat seat) {
        store.put(seat.getId(), seat);
    }

    @Override
    public Optional<ShowSeat> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ShowSeat> findByIds(List<String> ids) {
        List<ShowSeat> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            ShowSeat s = store.get(id);
            if (s != null) result.add(s);
        }
        return result;
    }

    @Override
    public List<ShowSeat> findByShowId(String showId) {
        return store.values().stream()
                .filter(s -> s.getShowId().equals(showId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowSeat> findExpiredLocks(LocalDateTime now) {
        return store.values().stream()
                .filter(s -> s.getStatus() == SeatStatus.LOCKED && s.isLockExpired(now))
                .collect(Collectors.toList());
    }
}
