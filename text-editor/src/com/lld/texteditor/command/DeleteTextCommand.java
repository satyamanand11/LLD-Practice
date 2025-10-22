package com.lld.texteditor.command;

import com.lld.texteditor.domain.entity.Document;
import com.lld.texteditor.domain.entity.Run;
import com.lld.texteditor.domain.value.DeletedSlice;
import com.lld.texteditor.manager.StyleManager;

public final class DeleteTextCommand implements EditCommand {
    private final int row, start, length;
    private DeletedSlice snapshot;

    public DeleteTextCommand(int row, int start, int length) {
        this.row = row;
        this.start = start;
        this.length = length;
    }

    @Override
    public void execute(Document doc, StyleManager styles) {
        snapshot = doc.rowAt(row).delete(start, length);
    }

    @Override
    public void unexecute(Document doc, StyleManager styles) {
        int c = snapshot.start();
        for (Run r : snapshot.runs()) {
            doc.rowAt(row).insert(c, r.text(), r.style());
            c += r.length();
        }
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.DELETE_TEXT;
    }

    @Override
    public Payload toEventPayload() {
        return new Payload.DeleteTextPayload(row, start, length);
    }
}
