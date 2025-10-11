package com.lld.elevator.events;

import com.lld.elevator.enums.EventType;

public interface Subscriber {
    void onEvent(EventType type, Object payload);
}
