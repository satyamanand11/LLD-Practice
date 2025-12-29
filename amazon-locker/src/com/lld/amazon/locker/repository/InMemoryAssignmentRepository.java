package com.lld.amazon.locker.repository;

import com.lld.amazon.locker.model.Assignment;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAssignmentRepository implements AssignmentRepository {

    private final ConcurrentHashMap<String, Assignment> assignments = new ConcurrentHashMap<>();

    @Override
    public Optional<Assignment> findById(String assignmentId) {
        return Optional.ofNullable(assignments.get(assignmentId));
    }

    @Override
    public void save(Assignment assignment) {
        assignments.put(assignment.getAssignmentId(), assignment);
    }
}
