package com.lld.texteditor.domain.entity;

import com.lld.texteditor.collab.enums.ChangeKind;
import com.lld.texteditor.domain.value.Style;
import com.lld.texteditor.domain.value.StyleDelta;

public interface Payload {
    record AddTextPayload(int row, int col, String text, Style style) implements Payload {
    }

    record DeleteTextPayload(int row, int start, int length) implements Payload {
    }

    record ApplyStylePayload(int row, int start, int length, StyleDelta delta) implements Payload {
    }

    record UndoRedoPayload(ChangeKind original,
                           Payload originalPayload) implements Payload {
    }
}
