package com.lld.job.scheduler.model;

import com.lld.job.scheduler.handler.TaskHandler;
import com.lld.job.scheduler.schedule.SchedulePolicy;

public class TaskDefinition {
    int taskId;
    String taskName;
    SchedulePolicy taskSchedule;
    TaskHandler taskHandler;
    TaskStatus taskStatus;

    public TaskDefinition(int taskId, String taskName, SchedulePolicy taskSchedule, TaskHandler taskHandler, TaskStatus taskStatus) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskSchedule = taskSchedule;
        this.taskHandler = taskHandler;
        this.taskStatus = taskStatus;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public SchedulePolicy getTaskSchedule() {
        return taskSchedule;
    }

    public TaskHandler getTaskHandler() {
        return taskHandler;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }
}
