package com.lld.job.scheduler.repository;

import com.lld.job.scheduler.model.TaskExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskExecutionRepository {

    private final ConcurrentMap<String, TaskExecution> executionStore = new ConcurrentHashMap<>();

    public void save(TaskExecution taskExecution) {
        executionStore.put(taskExecution.getExecutionId(), taskExecution);
    }

    public Optional<TaskExecution> findById(String executionId) {
        return Optional.ofNullable(executionStore.get(executionId));
    }

    public List<TaskExecution> findByTaskDefinitionId(String taskDefinitionId) {
        List<TaskExecution> result = new ArrayList<>();

        for (TaskExecution execution : executionStore.values()) {
            if (execution.getTaskDefinitionId().equals(taskDefinitionId)) {
                result.add(execution);
            }
        }

        return result;
    }

    public List<TaskExecution> findAll() {
        return new ArrayList<>(executionStore.values());
    }
}