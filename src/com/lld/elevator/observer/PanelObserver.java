package com.lld.elevator.observer;


import com.lld.elevator.enums.EventType;
import com.lld.elevator.events.Subscriber;

import static com.lld.elevator.enums.EventType.HALL_CALL;

public class PanelObserver implements Subscriber {
    @Override public void onEvent(EventType type, Object payload) {
        switch (type) {
            case HALL_CALL -> System.out.println("[Panel] Hall button lit " + payload);
            case ASSIGNMENT -> System.out.println("[Panel] Assignment " + payload);
            case SERVED -> System.out.println("[Panel] Hall button off " + payload);
            default -> {}
        }
    }
}
