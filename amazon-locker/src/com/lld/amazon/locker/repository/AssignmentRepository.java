package com.lld.amazon.locker.repository;

import com.lld.amazon.locker.model.Assignment;

import java.util.Optional;

public interface AssignmentRepository {
    Optional<Assignment> findById(String assignmentId);
    void save(Assignment assignment);
}
