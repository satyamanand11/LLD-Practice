package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.CleanupTask;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryCleanupTaskRepository implements CleanupTaskRepository {
    private final Map<Integer, CleanupTask> store = new ConcurrentHashMap<>();

    @Override
    public Optional<CleanupTask> findById(int taskId) {
        return Optional.ofNullable(store.get(taskId));
    }

    @Override
    public List<CleanupTask> findByHousekeeper(int housekeeperAccountId) {
        return store.values().stream()
                .filter(task -> task.getHousekeeperAccountId() != null &&
                               task.getHousekeeperAccountId() == housekeeperAccountId)
                .collect(Collectors.toList());
    }

    @Override
    public void save(CleanupTask task) {
        store.put(task.getTaskId(), task);
    }
}

