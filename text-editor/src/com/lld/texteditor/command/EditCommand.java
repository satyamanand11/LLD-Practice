package com.lld.texteditor.command;

import com.lld.texteditor.domain.entity.Document;
import com.lld.texteditor.manager.StyleManager;

public interface EditCommand {
    void execute(Document doc, StyleManager styles);
    void unexecute(Document doc, StyleManager styles);
    ChangeKind kind();
    Payload toEventPayload();
}
