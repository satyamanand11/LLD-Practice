package com.lld.texteditor.domain;

public record DocumentId(int value) {
    public DocumentId {
        if (value < 0) throw new IllegalArgumentException("DocumentId must be non-negative");
    }
}