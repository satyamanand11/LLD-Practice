package com.lld.bms.domain;

public class Seat {
    private final String id;
    private final int row;
    private final int column;
    private final SeatType type;

    public Seat(String id, int row, int column, SeatType type) {
        this.id = id;
        this.row = row;
        this.column = column;
        this.type = type;
    }

    public String getId() { return id; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public SeatType getType() { return type; }
}
