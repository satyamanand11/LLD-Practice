package com.lld.texteditor.collab;

import com.lld.texteditor.domain.entity.DocumentId;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CollaborationSession {
    private final DocumentId docId;
    private final Set<String> viewers = ConcurrentHashMap.newKeySet();
    private volatile String lockHolder;

    public CollaborationSession(DocumentId id) {
        this.docId = id;
    }

    public void open(String user) {
        viewers.add(user);
    }

    public void close(String user) {
        viewers.remove(user);
        if (Objects.equals(lockHolder, user)) lockHolder = null;
    }

    public boolean acquire(String user) {
        if (lockHolder == null || lockHolder.equals(user)) {
            lockHolder = user;
            return true;
        }
        return false;
    }

    public void release(String user) {
        if (Objects.equals(lockHolder, user)) lockHolder = null;
    }

    public String lockHolder() {
        return lockHolder;
    }

    public Set<String> viewers() {
        return Collections.unmodifiableSet(viewers);
    }
}
