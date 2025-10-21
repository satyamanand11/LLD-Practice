package com.lld.texteditor.strategy;

public final class RunBufferStorage implements TextStorage {
    private final List<Run> runs = new ArrayList<>();

    public int length() {
        return runs.stream().mapToInt(Run::length).sum();
    }

    public String readPlain() {
        return runs.stream().map(r -> r.text.toString()).collect(Collectors.joining());
    }

    public CharWithStyle getAt(int col) { /* locate run; return char+style */ }

    public void insert(int col, String text, Style style) { /* split run at col; insert; coalesce */ }

    public DeletedSlice delete(int start, int length) { /* split boundaries; remove; return slice */ }

    public void applyStyle(int start, int length, StyleDelta delta) { /* split; restyle; coalesce */ }
}
