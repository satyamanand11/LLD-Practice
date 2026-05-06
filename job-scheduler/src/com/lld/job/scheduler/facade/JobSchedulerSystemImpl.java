package com.lld.job.scheduler.facade;

import com.lld.job.scheduler.config.SchedulerConfig;
import com.lld.job.scheduler.handler.TaskHandler;
import com.lld.job.scheduler.model.TaskDefinition;
import com.lld.job.scheduler.model.TaskExecution;
import com.lld.job.scheduler.model.TaskStatus;
import com.lld.job.scheduler.repository.TaskDefinitionRepository;
import com.lld.job.scheduler.repository.TaskExecutionRepository;
import com.lld.job.scheduler.schedule.SchedulePolicy;
import com.lld.job.scheduler.service.TaskExecutionService;
import com.lld.job.scheduler.service.TaskSchedulerService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default wiring of {@link JobSchedulerSystem}.
 *
 * In production this wiring would be done via DI. For an LLD interview
 * the facade itself owns construction of the collaborators.
 */
public class JobSchedulerSystemImpl implements JobSchedulerSystem {

    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final TaskSchedulerService taskSchedulerService;

    public JobSchedulerSystemImpl(SchedulerConfig schedulerConfig) {
        this.taskDefinitionRepository = new TaskDefinitionRepository();
        this.taskExecutionRepository = new TaskExecutionRepository();

        TaskExecutionService taskExecutionService = new TaskExecutionService(
                taskDefinitionRepository,
                taskExecutionRepository
        );

        this.taskSchedulerService = new TaskSchedulerService(
                schedulerConfig,
                taskDefinitionRepository,
                taskExecutionService
        );
    }

    @Override
    public void start() {
        taskSchedulerService.start();
    }

    @Override
    public void stop() {
        taskSchedulerService.stop();
    }

    @Override
    public String scheduleTask(String taskName, SchedulePolicy schedule, TaskHandler taskHandler) {
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("Task name is required");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule policy is required");
        }
        if (taskHandler == null) {
            throw new IllegalArgumentException("Task handler is required");
        }

        String taskId = UUID.randomUUID().toString();

        TaskDefinition taskDefinition = new TaskDefinition(
                taskId,
                taskName,
                schedule,
                taskHandler,
                TaskStatus.ACTIVE
        );

        taskSchedulerService.schedule(taskDefinition);
        return taskId;
    }

    @Override
    public boolean cancelTask(String taskId) {
        return taskSchedulerService.cancel(taskId);
    }

    @Override
    public boolean pauseTask(String taskId) {
        return taskSchedulerService.pause(taskId);
    }

    @Override
    public boolean resumeTask(String taskId) {
        return taskSchedulerService.resume(taskId);
    }

    @Override
    public Optional<TaskDefinition> getTask(String taskId) {
        return taskDefinitionRepository.findById(taskId);
    }

    @Override
    public List<TaskExecution> getExecutions(String taskId) {
        return taskExecutionRepository.findByTaskDefinitionId(taskId);
    }
}
