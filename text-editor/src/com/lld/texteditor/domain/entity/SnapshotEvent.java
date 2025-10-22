package com.lld.texteditor.domain.entity;

import java.time.Instant;
import java.util.List;

public record SnapshotEvent(DocumentId docId, long version, Instant ts, List<String> lines) implements DocEvent {
}
