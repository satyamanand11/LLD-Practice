package com.lld.texteditor.history;

public final class UndoRedoHistory {
    private final Deque<EditCommand> undo = new ArrayDeque<>();
    private final Deque<EditCommand> redo = new ArrayDeque<>();

    public void pushUndo(EditCommand c) {
        undo.push(c);
    }

    public Optional<EditCommand> popUndo() {
        return undo.isEmpty() ? Optional.empty() : Optional.of(undo.pop());
    }

    public void pushRedo(EditCommand c) {
        redo.push(c);
    }

    public Optional<EditCommand> popRedo() {
        return redo.isEmpty() ? Optional.empty() : Optional.of(redo.pop());
    }

    public void clearRedo() {
        redo.clear();
    }
}
