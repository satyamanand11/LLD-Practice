package com.lld.connect4.domain.game;

import com.lld.connect4.domain.board.Board;
import com.lld.connect4.domain.board.Position;
import com.lld.connect4.domain.rules.WinChecker;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing a Connect4 game.
 * Encapsulates game state and business logic.
 * Follows DDD principles and Single Responsibility Principle.
 */
public class Game {
    private final GameId id;
    private final Board board;
    private final WinChecker winChecker;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private GameState state;
    private Player winner;
    private WinningLine winningLine;
    private final Instant createdAt;
    private Instant updatedAt;

    public Game(GameId id, Board board, WinChecker winChecker) {
        if (id == null) {
            throw new IllegalArgumentException("Game ID cannot be null");
        }
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }
        if (winChecker == null) {
            throw new IllegalArgumentException("WinChecker cannot be null");
        }
        this.id = id;
        this.board = board;
        this.winChecker = winChecker;
        this.state = GameState.WAITING_FOR_PLAYERS;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Adds the first player to the game.
     */
    public void addPlayer1(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (player1 != null) {
            throw new IllegalStateException("Player 1 already set");
        }
        if (state != GameState.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot add players to a game that has started");
        }
        this.player1 = player;
        updateState();
        this.updatedAt = Instant.now();
    }

    /**
     * Adds the second player to the game.
     */
    public void addPlayer2(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (player2 != null) {
            throw new IllegalStateException("Player 2 already set");
        }
        if (state != GameState.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot add players to a game that has started");
        }
        if (player1 == null) {
            throw new IllegalStateException("Player 1 must be added first");
        }
        if (player.getDisc() == player1.getDisc()) {
            throw new IllegalArgumentException("Players must have different disc colors");
        }
        this.player2 = player;
        this.currentPlayer = player1; // Player 1 starts
        updateState();
        this.updatedAt = Instant.now();
    }

    /**
     * Makes a move by placing a disc in the specified column.
     */
    public MoveResult makeMove(PlayerId playerId, int column) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }
        if (state != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress. Current state: " + state);
        }
        if (!currentPlayer.getId().equals(playerId)) {
            throw new IllegalStateException("It's not this player's turn");
        }

        if (board.isColumnFull(column)) {
            return MoveResult.invalid();
        }

        int row = board.placeDisc(column, currentPlayer.getDisc());
        if (row == -1) {
            return MoveResult.invalid();
        }

        Position position = new Position(row, column);
        this.updatedAt = Instant.now();

        // Check for win
        WinningLine winningLine = winChecker.checkWin(board, position, currentPlayer.getDisc());
        if (winningLine != null) {
            this.winner = currentPlayer;
            this.winningLine = winningLine;
            this.state = GameState.FINISHED_WIN;
            return MoveResult.winning(position, winningLine);
        }

        // Check for draw
        if (board.isFull()) {
            this.state = GameState.FINISHED_DRAW;
            return MoveResult.draw(position);
        }

        // Switch to next player
        switchPlayer();
        return MoveResult.valid(position);
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void updateState() {
        if (player1 != null && player2 != null) {
            this.state = GameState.IN_PROGRESS;
        }
    }

    public GameId getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getState() {
        return state;
    }

    public Player getWinner() {
        return winner;
    }

    public WinningLine getWinningLine() {
        return winningLine;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

