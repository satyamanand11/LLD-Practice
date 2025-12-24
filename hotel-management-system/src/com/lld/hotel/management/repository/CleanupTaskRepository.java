package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.CleanupTask;

import java.util.List;
import java.util.Optional;

public interface CleanupTaskRepository {
    Optional<CleanupTask> findById(int taskId);
    List<CleanupTask> findByHousekeeper(int housekeeperAccountId);
    void save(CleanupTask task);
}
