package com.lld.job.scheduler.model;


import java.time.Instant;

public class TaskExecution {

    private final String executionId;
    private final String taskDefinitionId;
    private final Instant scheduledTime;

    private Instant actualStartTime;
    private Instant completedAt;
    private ExecutionStatus status;
    private String errorMessage;

    public TaskExecution(
            String executionId,
            String taskDefinitionId,
            Instant scheduledTime
    ) {
        this.executionId = executionId;
        this.taskDefinitionId = taskDefinitionId;
        this.scheduledTime = scheduledTime;
        this.status = ExecutionStatus.CREATED;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markRunning(Instant actualStartTime) {
        this.actualStartTime = actualStartTime;
        this.status = ExecutionStatus.RUNNING;
    }

    public void markSuccess(Instant completedAt) {
        this.completedAt = completedAt;
        this.status = ExecutionStatus.SUCCESS;
    }

    public void markFailed(Instant completedAt, Exception exception) {
        this.completedAt = completedAt;
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = exception.getMessage();
    }

    public void markSkipped(Instant completedAt, String reason) {
        this.completedAt = completedAt;
        this.status = ExecutionStatus.SKIPPED;
        this.errorMessage = reason;
    }
}
