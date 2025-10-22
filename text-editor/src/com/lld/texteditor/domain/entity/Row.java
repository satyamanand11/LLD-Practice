package com.lld.texteditor.domain.entity;

import com.lld.texteditor.domain.value.CharWithStyle;
import com.lld.texteditor.domain.value.Style;
import com.lld.texteditor.domain.StyleDelta;
import com.lld.texteditor.domain.value.DeletedSlice;
import com.lld.texteditor.strategy.TextStorage;

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
