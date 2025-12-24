package com.lld.hotel.management.entities;

import java.time.LocalDateTime;

public class CleanupTask {

    public enum Status {
        CREATED,
        ASSIGNED,
        COMPLETED
    }

    private final int taskId;
    private final int roomId;

    private Integer housekeeperAccountId;
    private Status status;

    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public CleanupTask(int taskId, int roomId) {
        if (taskId <= 0) throw new IllegalArgumentException("taskId must be positive");
        if (roomId <= 0) throw new IllegalArgumentException("roomId must be positive");
        this.taskId = taskId;
        this.roomId = roomId;
        this.status = Status.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public void assignToHousekeeper(int housekeeperAccountId) {
        if (status != Status.CREATED) throw new IllegalStateException("Assign only from CREATED");
        if (housekeeperAccountId <= 0) throw new IllegalArgumentException("housekeeperAccountId must be positive");
        this.housekeeperAccountId = housekeeperAccountId;
        this.status = Status.ASSIGNED;
    }

    public void complete() {
        if (status != Status.ASSIGNED) throw new IllegalStateException("Complete only from ASSIGNED");
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public int getTaskId() { return taskId; }
    public int getRoomId() { return roomId; }
    public Integer getHousekeeperAccountId() { return housekeeperAccountId; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
