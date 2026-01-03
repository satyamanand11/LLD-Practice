package com.lld.connect4.domain.game;

import com.lld.connect4.domain.board.Position;

/**
 * Value object representing the result of a move.
 * Follows DDD principles by encapsulating move outcome.
 */
public class MoveResult {
    private final boolean isValid;
    private final Position position;
    private final boolean isWinningMove;
    private final boolean isDraw;
    private final WinningLine winningLine;

    private MoveResult(boolean isValid, Position position, boolean isWinningMove, 
                      boolean isDraw, WinningLine winningLine) {
        this.isValid = isValid;
        this.position = position;
        this.isWinningMove = isWinningMove;
        this.isDraw = isDraw;
        this.winningLine = winningLine;
    }

    public static MoveResult invalid() {
        return new MoveResult(false, null, false, false, null);
    }

    public static MoveResult valid(Position position) {
        return new MoveResult(true, position, false, false, null);
    }

    public static MoveResult winning(Position position, WinningLine winningLine) {
        return new MoveResult(true, position, true, false, winningLine);
    }

    public static MoveResult draw(Position position) {
        return new MoveResult(true, position, false, true, null);
    }

    public boolean isValid() {
        return isValid;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isWinningMove() {
        return isWinningMove;
    }

    public boolean isDraw() {
        return isDraw;
    }

    public WinningLine getWinningLine() {
        return winningLine;
    }
}

