package com.lld.texteditor.manager;

import com.lld.texteditor.collab.DocumentEventListener;
import com.lld.texteditor.domain.entity.DocumentId;

import java.util.List;
import java.util.concurrent.Callable;

public interface EventBusManager {
    Subscription subscribe(DocumentId id, String userId, long fromVersion, DocumentEventListener l, Callable<List<String>> snapshotSupplier);
    void publish(DocEvent e);
    long currentVersion(DocumentId id);
    interface Subscription { void close(); }
}
