package com.lld.job.scheduler.service;


import com.lld.job.scheduler.model.ScheduledTask;
import com.lld.job.scheduler.model.TaskDefinition;
import com.lld.job.scheduler.model.TaskExecution;
import com.lld.job.scheduler.model.TaskExecutionContext;
import com.lld.job.scheduler.repository.TaskDefinitionRepository;
import com.lld.job.scheduler.repository.TaskExecutionRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class TaskExecutionService {

    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskExecutionRepository taskExecutionRepository;

    public TaskExecutionService(
            TaskDefinitionRepository taskDefinitionRepository,
            TaskExecutionRepository taskExecutionRepository
    ) {
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskExecutionRepository = taskExecutionRepository;
    }

    public void execute(ScheduledTask scheduledTask) {
        Optional<TaskDefinition> optionalTask =
                taskDefinitionRepository.findById(scheduledTask.getTaskDefinitionId());

        String executionId = UUID.randomUUID().toString();

        TaskExecution taskExecution = new TaskExecution(
                executionId,
                scheduledTask.getTaskDefinitionId(),
                scheduledTask.getExecutionTime()
        );

        taskExecutionRepository.save(taskExecution);

        if (optionalTask.isEmpty()) {
            taskExecution.markSkipped(Instant.now(), "Task definition not found");
            taskExecutionRepository.save(taskExecution);
            return;
        }

        TaskDefinition taskDefinition = optionalTask.get();

        if (!taskDefinition.isActive()) {
            taskExecution.markSkipped(Instant.now(), "Task is not active");
            taskExecutionRepository.save(taskExecution);
            return;
        }

        Instant actualStartTime = Instant.now();
        taskExecution.markRunning(actualStartTime);
        taskExecutionRepository.save(taskExecution);

        TaskExecutionContext context = new TaskExecutionContext(
                executionId,
                taskDefinition.getTaskId(),
                scheduledTask.getExecutionTime(),
                actualStartTime,
                1
        );

        try {
            taskDefinition.getTaskHandler().execute(context);
            taskExecution.markSuccess(Instant.now());
        } catch (Exception exception) {
            taskExecution.markFailed(Instant.now(), exception);
        }

        taskExecutionRepository.save(taskExecution);
    }
}
