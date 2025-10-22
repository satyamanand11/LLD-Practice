package com.lld.texteditor.collab;

import com.lld.texteditor.domain.entity.ChangeEvent;
import com.lld.texteditor.domain.entity.LockEvent;

public interface DocumentEventListener {
    void onChange(ChangeEvent e);
    void onLockChanged(LockEvent e);
    void onSnapshot(SnapshotEvent e);
}
