package com.lld.texteditor.domain;

public final class Row {
    private final TextStorage buffer;

    public Row(TextStorage buffer) {
        this.buffer = buffer;
    }

    public int length() {
        return buffer.length();
    }

    public String readPlain() {
        return buffer.readPlain();
    }

    public CharWithStyle getAt(int col) {
        return buffer.getAt(col);
    }

    // package-private: used by commands
    void insert(int col, String text, Style style) {
        buffer.insert(col, text, style);
    }

    DeletedSlice delete(int start, int length) {
        return buffer.delete(start, length);
    }

    void applyStyle(int start, int length, StyleDelta delta) {
        buffer.applyStyle(start, length, delta);
    }
}
