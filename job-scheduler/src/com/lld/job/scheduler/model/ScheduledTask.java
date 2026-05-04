package com.lld.job.scheduler.model;

import java.time.Instant;

public class ScheduledTask {
    String taskDefinitionId;
    Instant executionTime;

    public ScheduledTask(String taskDefinitionId, Instant executionTime) {
        this.taskDefinitionId = taskDefinitionId;
        this.executionTime = executionTime;
    }

    public String getTaskDefinitionId() {
        return taskDefinitionId;
    }

    public Instant getExecutionTime() {
        return executionTime;
    }
}
