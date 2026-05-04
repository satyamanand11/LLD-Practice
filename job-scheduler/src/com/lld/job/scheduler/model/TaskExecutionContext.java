package com.lld.job.scheduler.model;

import java.time.Instant;

public class TaskExecutionContext {
     String executionId;
     String taskDefinitionId;
     Instant scheduledTime;
     Instant actualStartTime;
     int attemptNumber;

    public TaskExecutionContext(String executionId, String taskDefinitionId, Instant scheduledTime, Instant actualStartTime, int attemptNumber) {
        this.executionId = executionId;
        this.taskDefinitionId = taskDefinitionId;
        this.scheduledTime = scheduledTime;
        this.actualStartTime = actualStartTime;
        this.attemptNumber = attemptNumber;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getTaskDefinitionId() {
        return taskDefinitionId;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public Instant getActualStartTime() {
        return actualStartTime;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }
}
