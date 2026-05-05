package com.lld.job.scheduler.repository;

import com.lld.job.scheduler.model.TaskDefinition;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskDefinitionRepository {

    private final ConcurrentMap<String, TaskDefinition> taskStore = new ConcurrentHashMap<>();

    public void save(TaskDefinition taskDefinition) {
        taskStore.put(taskDefinition.getTaskId(), taskDefinition);
    }

    public Optional<TaskDefinition> findById(String taskId) {
        return Optional.ofNullable(taskStore.get(taskId));
    }

    public Collection<TaskDefinition> findAll() {
        return taskStore.values();
    }

    public void delete(String taskId) {
        taskStore.remove(taskId);
    }

    public boolean existsById(String taskId) {
        return taskStore.containsKey(taskId);
    }
}
