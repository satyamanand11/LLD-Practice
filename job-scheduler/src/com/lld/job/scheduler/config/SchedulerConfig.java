package com.lld.job.scheduler.config;

public class SchedulerConfig {

    private final int workerThreadCount;
    private final TaskPriorityPolicy taskPriorityPolicy;

    public SchedulerConfig(int workerThreadCount, TaskPriorityPolicy taskPriorityPolicy) {
        if (workerThreadCount <= 0) {
            throw new IllegalArgumentException("Worker thread count must be positive");
        }

        if (taskPriorityPolicy == null) {
            throw new IllegalArgumentException("Task priority policy cannot be null");
        }

        this.workerThreadCount = workerThreadCount;
        this.taskPriorityPolicy = taskPriorityPolicy;
    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public TaskPriorityPolicy getTaskPriorityPolicy() {
        return taskPriorityPolicy;
    }
}
