package com.lld.texteditor.domain.entity;

import java.time.Instant;

public interface DocEvent {
    DocumentId docId();

    long version();

    Instant ts();
}
