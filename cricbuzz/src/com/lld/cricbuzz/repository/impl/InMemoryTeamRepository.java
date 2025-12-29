package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.team.Team;
import com.lld.cricbuzz.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTeamRepository implements TeamRepository {
    private final Map<String, Team> teams = new ConcurrentHashMap<>();

    @Override
    public void save(Team team) {
        teams.put(team.getTeamId(), team);
    }

    @Override
    public Optional<Team> findById(String teamId) {
        return Optional.ofNullable(teams.get(teamId));
    }

    @Override
    public List<Team> findAll() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public void delete(String teamId) {
        teams.remove(teamId);
    }
}

