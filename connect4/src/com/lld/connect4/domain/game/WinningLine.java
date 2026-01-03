package com.lld.connect4.domain.game;

import com.lld.connect4.domain.board.Position;

import java.util.List;
import java.util.Objects;

/**
 * Value object representing a winning line of 4 connected discs.
 * Immutable and follows DDD principles.
 */
public class WinningLine {
    private final List<Position> positions;
    private final Disc disc;

    public WinningLine(List<Position> positions, Disc disc) {
        if (positions == null || positions.size() != 4) {
            throw new IllegalArgumentException("Winning line must contain exactly 4 positions");
        }
        if (disc == null) {
            throw new IllegalArgumentException("Disc cannot be null");
        }
        this.positions = List.copyOf(positions);
        this.disc = disc;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public Disc getDisc() {
        return disc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WinningLine that = (WinningLine) o;
        return Objects.equals(positions, that.positions) && disc == that.disc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions, disc);
    }

    @Override
    public String toString() {
        return "WinningLine{" +
                "positions=" + positions +
                ", disc=" + disc +
                '}';
    }
}

