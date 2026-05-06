package com.lld.job.scheduler.facade;

import com.lld.job.scheduler.handler.TaskHandler;
import com.lld.job.scheduler.model.TaskDefinition;
import com.lld.job.scheduler.model.TaskExecution;
import com.lld.job.scheduler.schedule.SchedulePolicy;

import java.util.List;
import java.util.Optional;

/**
 * Public API of the job scheduler system.
 * Entry point for clients / controllers.
 *
 * Characteristics:
 *  - Thin delegation layer, no business logic.
 *  - Hides internal services, repositories and worker pool wiring.
 */
public interface JobSchedulerSystem {

    /**
     * Starts the dispatcher thread and worker pool.
     */
    void start();

    /**
     * Gracefully stops the dispatcher and worker pool.
     */
    void stop();

    /**
     * Registers a new task with the given schedule and handler.
     *
     * @param taskName     human readable name of the task
     * @param schedule     schedule policy (one-time / fixed-interval / cron)
     * @param taskHandler  handler that executes the task body
     * @return the generated task id
     */
    String scheduleTask(String taskName, SchedulePolicy schedule, TaskHandler taskHandler);

    /**
     * Cancels a previously scheduled task. Pending executions are removed
     * from the queue and any in-flight execution is allowed to complete.
     */
    boolean cancelTask(String taskId);

    /**
     * Pauses a task. Future scheduled fire-times will be skipped until resumed.
     */
    boolean pauseTask(String taskId);

    /**
     * Resumes a paused task. Re-arms the next scheduled fire-time.
     */
    boolean resumeTask(String taskId);

    /**
     * Returns the task definition for inspection.
     */
    Optional<TaskDefinition> getTask(String taskId);

    /**
     * Returns execution history for a task.
     */
    List<TaskExecution> getExecutions(String taskId);
}
