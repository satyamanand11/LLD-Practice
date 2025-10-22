package com.lld.texteditor.collab;

import com.lld.texteditor.domain.entity.DocumentId;

public interface DocumentLockManager {
    void withRead(DocumentId id, Runnable r);
    void withWrite(DocumentId id, Runnable r);
}
