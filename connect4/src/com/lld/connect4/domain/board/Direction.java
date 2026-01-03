package com.lld.connect4.domain.board;

/**
 * Enum representing directions for checking winning lines.
 * Used in win checking algorithm.
 */
public enum Direction {
    HORIZONTAL(0, 1),      // Right
    VERTICAL(1, 0),        // Down
    DIAGONAL_DOWN(1, 1),   // Down-right
    DIAGONAL_UP(-1, 1);    // Up-right

    private final int rowDelta;
    private final int colDelta;

    Direction(int rowDelta, int colDelta) {
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
    }

    public int getRowDelta() {
        return rowDelta;
    }

    public int getColDelta() {
        return colDelta;
    }
}

