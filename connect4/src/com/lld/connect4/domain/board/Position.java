package com.lld.connect4.domain.board;

import java.util.Objects;

/**
 * Value object representing a position on the Connect4 board.
 * Immutable and follows DDD principles.
 */
public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Row and column must be non-negative");
        }
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Position move(Direction direction) {
        return new Position(row + direction.getRowDelta(), col + direction.getColDelta());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}

