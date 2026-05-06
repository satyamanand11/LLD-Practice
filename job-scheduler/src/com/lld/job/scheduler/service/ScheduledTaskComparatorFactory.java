package com.lld.job.scheduler.service;

import com.lld.job.scheduler.config.TaskPriorityPolicy;
import com.lld.job.scheduler.model.ScheduledTask;

import java.util.Comparator;

public final class ScheduledTaskComparatorFactory {

    private ScheduledTaskComparatorFactory() {
    }

    public static Comparator<ScheduledTask> getComparator(TaskPriorityPolicy policy) {
        switch (policy) {
            case EARLIEST_EXECUTION_TIME_FIRST:
                return Comparator.comparing(ScheduledTask::getExecutionTime);
            case LATEST_EXECUTION_TIME_FIRST:
                return Comparator.comparing(ScheduledTask::getExecutionTime).reversed();
            default:
                throw new IllegalArgumentException(
                        "Unsupported task priority policy: " + policy
                );
        }
    }
}
