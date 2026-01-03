package com.lld.connect4.domain.rules;

import com.lld.connect4.domain.board.Board;
import com.lld.connect4.domain.board.Direction;
import com.lld.connect4.domain.board.Position;
import com.lld.connect4.domain.game.Disc;
import com.lld.connect4.domain.game.WinningLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for checking winning conditions in Connect4.
 * Follows Strategy pattern and Single Responsibility Principle.
 */
public class WinChecker {
    private static final int WINNING_LENGTH = 4;

    /**
     * Checks if placing a disc at the given position results in a win.
     * Returns the winning line if found, null otherwise.
     */
    public WinningLine checkWin(Board board, Position position, Disc disc) {
        if (board == null || position == null || disc == null) {
            return null;
        }

        // Check all directions from the placed position
        for (Direction direction : Direction.values()) {
            WinningLine winningLine = checkDirection(board, position, disc, direction);
            if (winningLine != null) {
                return winningLine;
            }
        }

        return null;
    }

    /**
     * Checks for a winning line in a specific direction.
     * Checks both positive and negative directions from the start position.
     */
    private WinningLine checkDirection(Board board, Position start, Disc disc, Direction direction) {
        int count = 1; // Count includes the starting position

        // Check in positive direction (forward)
        int forwardCount = countForward(board, start, disc, direction);
        count += forwardCount;

        // Check in negative direction (backward)
        int backwardCount = countBackward(board, start, disc, direction);
        count += backwardCount;

        if (count >= WINNING_LENGTH) {
            // Build the winning line positions
            List<Position> winningPositions = new ArrayList<>();
            
            // Add backward positions (in reverse order)
            Position current = start;
            for (int i = 0; i < backwardCount; i++) {
                current = getOppositePosition(current, direction);
                winningPositions.add(0, current); // Add to beginning
            }
            
            // Add the starting position
            winningPositions.add(start);
            
            // Add forward positions
            current = start;
            for (int i = 0; i < forwardCount; i++) {
                current = current.move(direction);
                winningPositions.add(current);
            }
            
            // Return exactly 4 positions
            List<Position> result = new ArrayList<>();
            for (int i = 0; i < WINNING_LENGTH && i < winningPositions.size(); i++) {
                result.add(winningPositions.get(i));
            }
            return new WinningLine(result, disc);
        }

        return null;
    }

    /**
     * Counts consecutive discs in the forward direction.
     */
    private int countForward(Board board, Position start, Disc disc, Direction direction) {
        int count = 0;
        Position current = start.move(direction);
        while (board.isValidPosition(current) && 
               disc.equals(board.getDisc(current))) {
            count++;
            current = current.move(direction);
        }
        return count;
    }

    /**
     * Counts consecutive discs in the backward direction.
     */
    private int countBackward(Board board, Position start, Disc disc, Direction direction) {
        int count = 0;
        Position current = getOppositePosition(start, direction);
        while (board.isValidPosition(current) && 
               disc.equals(board.getDisc(current))) {
            count++;
            current = getOppositePosition(current, direction);
        }
        return count;
    }

    /**
     * Gets the position in the opposite direction.
     */
    private Position getOppositePosition(Position position, Direction direction) {
        return new Position(
            position.getRow() - direction.getRowDelta(),
            position.getCol() - direction.getColDelta()
        );
    }
}

