package com.lld.connect4.domain.board;

import com.lld.connect4.domain.game.Disc;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing the Connect4 game board.
 * Encapsulates board state and provides operations for placing discs.
 * Follows Single Responsibility Principle.
 */
public class Board {
    private static final int DEFAULT_ROWS = 6;
    private static final int DEFAULT_COLS = 7;
    private static final int WINNING_LENGTH = 4;

    private final int rows;
    private final int cols;
    private final Disc[][] grid;
    private int moveCount;

    public Board() {
        this(DEFAULT_ROWS, DEFAULT_COLS);
    }

    public Board(int rows, int cols) {
        if (rows < WINNING_LENGTH || cols < WINNING_LENGTH) {
            throw new IllegalArgumentException(
                "Board must be at least " + WINNING_LENGTH + "x" + WINNING_LENGTH);
        }
        this.rows = rows;
        this.cols = cols;
        this.grid = new Disc[rows][cols];
        this.moveCount = 0;
    }

    /**
     * Places a disc in the specified column.
     * Returns the row where the disc was placed, or -1 if column is full.
     */
    public int placeDisc(int col, Disc disc) {
        if (col < 0 || col >= cols) {
            throw new IllegalArgumentException("Column " + col + " is out of bounds");
        }
        if (disc == null) {
            throw new IllegalArgumentException("Disc cannot be null");
        }

        // Find the lowest available row in the column
        for (int row = rows - 1; row >= 0; row--) {
            if (grid[row][col] == null) {
                grid[row][col] = disc;
                moveCount++;
                return row;
            }
        }
        return -1; // Column is full
    }

    public Disc getDisc(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return null;
        }
        return grid[row][col];
    }

    public Disc getDisc(Position position) {
        return getDisc(position.getRow(), position.getCol());
    }

    public boolean isColumnFull(int col) {
        return grid[0][col] != null;
    }

    public boolean isFull() {
        return moveCount >= rows * cols;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Returns a copy of the board state for display purposes.
     */
    public Disc[][] getGridCopy() {
        Disc[][] copy = new Disc[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    /**
     * Checks if a position is within board bounds.
     */
    public boolean isValidPosition(Position position) {
        return position.getRow() >= 0 && position.getRow() < rows &&
               position.getCol() >= 0 && position.getCol() < cols;
    }
}

