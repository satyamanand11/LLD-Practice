package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.player.Player;
import com.lld.cricbuzz.domain.player.PlayerRole;
import com.lld.cricbuzz.repository.PlayerRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing players
 */
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(String name, int age, String country, PlayerRole role) {
        String playerId = "PLAYER_" + UUID.randomUUID().toString().substring(0, 8);
        Player player = new Player(playerId, name, age, country, role);
        playerRepository.save(player);
        return player;
    }

    public void updateRole(String playerId, PlayerRole role) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        player.updateRole(role);
        playerRepository.save(player);
    }

    public Player getPlayer(String playerId) {
        return playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public List<Player> getPlayersByTeam(String teamId) {
        return playerRepository.findByTeamId(teamId);
    }
}

