package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.CleanupTask;
import com.lld.hotel.management.entities.IdGenerator;
import com.lld.hotel.management.observer.EventBus;
import com.lld.hotel.management.repository.CleanupTaskRepository;

import java.util.List;

/**
 * CleanupTaskService (R7)
 * Manages cleanup tasks for rooms
 */
public class CleanupTaskService {
    private final CleanupTaskRepository taskRepository;
    private final EventBus eventBus;

    public CleanupTaskService(CleanupTaskRepository taskRepository, EventBus eventBus) {
        this.taskRepository = taskRepository;
        this.eventBus = eventBus;
    }

    // EventBus is reserved for future event publishing (Observer pattern)
    @SuppressWarnings("unused")
    private EventBus getEventBus() {
        return eventBus;
    }

    public CleanupTask createCleanupTask(int roomId) {
        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }

        CleanupTask task = new CleanupTask(IdGenerator.nextId(), roomId);
        taskRepository.save(task);
        return task;
    }

    public void assignTask(int taskId, int housekeeperAccountId) {
        if (housekeeperAccountId <= 0) {
            throw new IllegalArgumentException("housekeeperAccountId must be positive");
        }

        CleanupTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.assignToHousekeeper(housekeeperAccountId);
        taskRepository.save(task);

        // Publish event for notification (Observer pattern)
        // In a real system, this would trigger CleanupTaskAssignedEvent
    }

    public void completeTask(int taskId) {
        CleanupTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.complete();
        taskRepository.save(task);

        // Publish event for notification (Observer pattern)
        // In a real system, this would trigger CleanupTaskCompletedEvent
    }

    public List<CleanupTask> getTasksByHousekeeper(int housekeeperAccountId) {
        return taskRepository.findByHousekeeper(housekeeperAccountId);
    }

    public CleanupTask getTask(int taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }
}

