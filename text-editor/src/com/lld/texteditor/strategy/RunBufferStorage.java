package com.lld.texteditor.strategy;

import com.lld.texteditor.domain.value.CharWithStyle;
import com.lld.texteditor.domain.entity.Run;
import com.lld.texteditor.domain.value.Style;
import com.lld.texteditor.domain.value.DeletedSlice;
import com.lld.texteditor.domain.value.StyleDelta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RunBufferStorage implements TextStorage {
    private final List<Run> runs = new ArrayList<>();

    public int length() {
        return runs.stream().mapToInt(Run::length).sum();
    }

    public String readPlain() {
        return runs.stream().map(r -> r.text.toString()).collect(Collectors.joining());
    }

    public CharWithStyle getAt(int col) {
        return null;
    }

    public void insert(int col, String text, Style style) { /* split run at col; insert; coalesce */ }

    public DeletedSlice delete(int start, int length) { /* split boundaries; remove; return slice */
        return null;
    }

    public void applyStyle(int start, int length, StyleDelta delta) { /* split; restyle; coalesce */ }
}
