package com.lld.texteditor.strategy;

public final class DeletedSlice {
    public final int start; // original start column
    public final List<Run> runs; // exact styled runs removed

    public DeletedSlice(int start, List<Run> runs) {
        this.start = start;
        this.runs = List.copyOf(runs);
    }
}