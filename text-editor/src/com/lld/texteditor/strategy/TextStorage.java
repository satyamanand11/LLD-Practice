package com.lld.texteditor.strategy;

import com.lld.texteditor.domain.value.CharWithStyle;
import com.lld.texteditor.domain.value.Style;
import com.lld.texteditor.domain.value.DeletedSlice;
import com.lld.texteditor.domain.value.StyleDelta;

public interface TextStorage {
    int length();

    String readPlain();

    CharWithStyle getAt(int col);

    void insert(int col, String text, Style style);

    DeletedSlice delete(int start, int length);

    void applyStyle(int start, int length, StyleDelta delta);
}
