package com.lld.texteditor.command;

import com.lld.texteditor.collab.enums.ChangeKind;
import com.lld.texteditor.domain.entity.Document;
import com.lld.texteditor.domain.entity.Payload;
import com.lld.texteditor.domain.value.Style;
import com.lld.texteditor.manager.StyleManager;

public final class AddTextCommand implements EditCommand {
    private final int row, col;
    private final String text;
    private final Style style;

    public AddTextCommand(int row, int col, String text, Style style) {
        this.row=row; this.col=col; this.text=text; this.style=style;
    }

    @Override public void execute(Document doc, StyleManager styles) {
        doc.ensureRowExists(row); doc.rowAt(row).insert(col, text, style);
    }
    @Override public void unexecute(com.example.editor.domain.entity.Document doc, StyleManager styles) {
        doc.rowAt(row).delete(col, text.length());
    }
    @Override public ChangeKind kind(){ return ChangeKind.ADD_TEXT; }
    @Override public Payload toEventPayload(){ return new Payload.AddTextPayload(row,col,text,style); }
}
