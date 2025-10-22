package com.lld.texteditor.domain.entity;

import com.lld.texteditor.collab.enums.ChangeKind;

import java.time.Instant;

public record ChangeEvent(DocumentId docId, long version, Instant ts, String userId, ChangeKind kind,
                          Payload payload) implements DocEvent {
}
