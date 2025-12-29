package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.player.Player;
import com.lld.cricbuzz.repository.PlayerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPlayerRepository implements PlayerRepository {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, List<String>> teamPlayers = new ConcurrentHashMap<>(); // teamId -> playerIds

    @Override
    public void save(Player player) {
        players.put(player.getPlayerId(), player);
    }

    @Override
    public Optional<Player> findById(String playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    @Override
    public List<Player> findByTeamId(String teamId) {
        List<String> playerIds = teamPlayers.getOrDefault(teamId, new ArrayList<>());
        return playerIds.stream()
            .map(players::get)
            .filter(p -> p != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<Player> findAll() {
        return new ArrayList<>(players.values());
    }

    @Override
    public void delete(String playerId) {
        players.remove(playerId);
        teamPlayers.values().forEach(list -> list.remove(playerId));
    }

    public void addPlayerToTeam(String teamId, String playerId) {
        teamPlayers.computeIfAbsent(teamId, k -> new ArrayList<>()).add(playerId);
    }
}

