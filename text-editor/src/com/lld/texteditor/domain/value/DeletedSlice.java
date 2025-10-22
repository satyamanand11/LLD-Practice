package com.lld.texteditor.domain.value;

import com.lld.texteditor.domain.entity.Run;

import java.util.List;

public final class DeletedSlice {
    public final int start;
    public final List<Run> runs;

    public DeletedSlice(int start, List<Run> runs) {
        this.start = start;
        this.runs = List.copyOf(runs);
    }
}