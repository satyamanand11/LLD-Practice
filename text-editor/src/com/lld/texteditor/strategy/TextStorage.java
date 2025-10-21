package com.lld.texteditor.strategy;

public interface TextStorage {
    int length();

    String readPlain();

    CharWithStyle getAt(int col);

    // mutations
    void insert(int col, String text, Style style);

    DeletedSlice delete(int start, int length);

    void applyStyle(int start, int length, StyleDelta delta);
}
