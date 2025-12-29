package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.team.Team;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Team aggregate
 */
public interface TeamRepository {
    void save(Team team);
    Optional<Team> findById(String teamId);
    List<Team> findAll();
    void delete(String teamId);
}

