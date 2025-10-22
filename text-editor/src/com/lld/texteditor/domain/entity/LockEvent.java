package com.lld.texteditor.domain.entity;

import com.lld.texteditor.collab.enums.LockState;

import java.time.Instant;

public record LockEvent(DocumentId docId, long version, Instant ts, String lockHolder,
                        LockState state) implements DocEvent {
}
