package com.lld.texteditor.manager;

import com.lld.texteditor.collab.CollaborationSession;
import com.lld.texteditor.domain.entity.DocumentId;

import java.util.Set;

public interface CollaborationManager {
    CollaborationSession sessionOf(DocumentId id);

    void open(DocumentId id, String userId);

    boolean acquire(DocumentId id, String userId);

    void release(DocumentId id, String userId);

    String lockHolder(DocumentId id);

    Set<String> viewers(DocumentId id);
}
