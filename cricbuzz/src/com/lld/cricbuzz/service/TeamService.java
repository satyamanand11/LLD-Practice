package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.team.Team;
import com.lld.cricbuzz.repository.TeamRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing teams
 */
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team createTeam(String name, String country) {
        String teamId = "TEAM_" + UUID.randomUUID().toString().substring(0, 8);
        Team team = new Team(teamId, name, country);
        teamRepository.save(team);
        return team;
    }

    public void addPlayer(String teamId, String playerId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.addPlayer(playerId);
        teamRepository.save(team);
    }

    public void removePlayer(String teamId, String playerId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.removePlayer(playerId);
        teamRepository.save(team);
    }

    public Team getTeam(String teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new IllegalArgumentException("Team not found"));
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}

