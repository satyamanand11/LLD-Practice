package com.lld.texteditor.domain.entity;

import com.lld.texteditor.strategy.RunBufferStorage;

import java.util.ArrayList;
import java.util.List;

public final class Document {
    private final DocumentId id;
    private final List<Row> rows = new ArrayList<>();
    private long version = 0L;

    public Document(DocumentId id) {
        this.id = id;
    }

    public DocumentId id() {
        return id;
    }


    public int rowCount() {
        return rows.size();
    }

    public long version() {
        return version;
    }

    public long bumpVersion() {
        return ++version;
    }

    // reading
    public String readLine(int row) {
        ensureRowExists(row);
        return rows.get(row).readPlain();
    }

    public String getStyle(int row, int col) {
        ensureRowExists(row);
        var cws = rows.get(row).getAt(col);
        var s = cws.style();
        var ch = cws.ch();
        StringBuilder sb = new StringBuilder()
                .append(ch).append("-").append(s.fontName()).append("-").append(s.fontSize());
        if (s.bold()) sb.append("-b");
        if (s.italic()) sb.append("-i");
        return sb.toString();
    }

    // row access
    Row rowAt(int row) {
        ensureRowExists(row);
        return rows.get(row);
    }

    void ensureRowExists(int row) {
        while (rows.size() <= row) rows.add(new Row(new RunBufferStorage()));
    }
}
