package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.tournament.MatchFormat;
import com.lld.cricbuzz.domain.tournament.Tournament;
import com.lld.cricbuzz.domain.tournament.TournamentStatus;
import com.lld.cricbuzz.repository.TournamentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing tournaments
 * Single Responsibility: Tournament management operations
 */
public class TournamentService {
    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public Tournament createTournament(String name, MatchFormat format) {
        String tournamentId = "TOUR_" + UUID.randomUUID().toString().substring(0, 8);
        Tournament tournament = new Tournament(tournamentId, name, format);
        tournamentRepository.save(tournament);
        return tournament;
    }

    /**
     * Save a tournament created using the builder pattern
     */
    public Tournament saveTournament(Tournament tournament) {
        tournamentRepository.save(tournament);
        return tournament;
    }

    public void addTeam(String tournamentId, String teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.addTeam(teamId);
        tournamentRepository.save(tournament);
    }

    public void scheduleMatch(String tournamentId, String matchId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.addMatch(matchId);
        tournamentRepository.save(tournament);
    }

    public void updateStatus(String tournamentId, TournamentStatus status) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.updateStatus(status);
        tournamentRepository.save(tournament);
    }

    public void setDates(String tournamentId, LocalDate startDate, LocalDate endDate) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.setDates(startDate, endDate);
        tournamentRepository.save(tournament);
    }

    public Tournament getTournament(String tournamentId) {
        return tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }
}

