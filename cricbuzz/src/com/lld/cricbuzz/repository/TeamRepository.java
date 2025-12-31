package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.team.Team;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Team entity
 * 
 * Separate repository for Team entity allows:
 * - Independent team management
 * - Team-specific queries
 * - Better scalability
 * 
 * Note: Locking is handled at the service layer via LockManager.
 * Repositories remain simple data access layers.
 */
public interface TeamRepository {
    void save(Team team);
    Optional<Team> findById(String teamId);
    List<Team> findAll();
    void delete(String teamId);
}

