package com.lld.texteditor.command;

import com.lld.texteditor.domain.entity.Document;
import com.lld.texteditor.domain.entity.Run;
import com.lld.texteditor.domain.value.DeletedSlice;
import com.lld.texteditor.domain.value.StyleDelta;
import com.lld.texteditor.manager.StyleManager;

public final class ApplyStyleCommand implements EditCommand {
    private final int row, start, length;
    private final StyleDelta delta;
    private DeletedSlice before; // snapshot original runs

    public ApplyStyleCommand(int row, int start, int length, StyleDelta delta) {
        this.row = row;
        this.start = start;
        this.length = length;
        this.delta = delta;
    }

    @Override
    public void execute(Document doc, StyleManager styles) {
        // Snapshot original styled runs by delete-then-reinsert (cheap snapshot trick)
        before = doc.rowAt(row).delete(start, length);
        int c = start;
        for (Run r : before.runs()) {
            doc.rowAt(row).insert(c, r.text(), r.style());
            c += r.length();
        }
        doc.rowAt(row).applyStyle(start, length, delta, styles);
    }

    @Override
    public void unexecute(Document doc, StyleManager styles) {
        // Restore original
        doc.rowAt(row).delete(start, length);
        int c = start;
        for (Run r : before.runs()) {
            doc.rowAt(row).insert(c, r.text(), r.style());
            c += r.length();
        }
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.APPLY_STYLE;
    }

    @Override
    public Payload toEventPayload() {
        return new Payload.ApplyStylePayload(row, start, length, delta);
    }
}
