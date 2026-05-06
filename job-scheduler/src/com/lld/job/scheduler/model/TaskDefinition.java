package com.lld.job.scheduler.model;

import com.lld.job.scheduler.handler.TaskHandler;
import com.lld.job.scheduler.schedule.SchedulePolicy;

public class TaskDefinition {

    private final String taskId;
    private final String taskName;
    private final SchedulePolicy schedulePolicy;
    private final TaskHandler taskHandler;
    private volatile TaskStatus taskStatus;

    public TaskDefinition(
            String taskId,
            String taskName,
            SchedulePolicy schedulePolicy,
            TaskHandler taskHandler,
            TaskStatus taskStatus
    ) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.schedulePolicy = schedulePolicy;
        this.taskHandler = taskHandler;
        this.taskStatus = taskStatus;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public SchedulePolicy getSchedulePolicy() {
        return schedulePolicy;
    }

    public TaskHandler getTaskHandler() {
        return taskHandler;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public boolean isActive() {
        return taskStatus == TaskStatus.ACTIVE;
    }

    public void cancel() {
        this.taskStatus = TaskStatus.CANCELLED;
    }

    public void pause() {
        if (taskStatus == TaskStatus.ACTIVE) {
            this.taskStatus = TaskStatus.PAUSED;
        }
    }

    public void resume() {
        if (taskStatus == TaskStatus.PAUSED) {
            this.taskStatus = TaskStatus.ACTIVE;
        }
    }

    public void markCompleted() {
        this.taskStatus = TaskStatus.COMPLETED;
    }
}
