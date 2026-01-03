package com.lld.connect4.domain.game;

/**
 * Enum representing the disc/coin color in Connect4.
 * Each player has a unique disc color.
 */
public enum Disc {
    RED('R'),
    YELLOW('Y');

    private final char symbol;

    Disc(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public Disc getOpposite() {
        return this == RED ? YELLOW : RED;
    }
}

