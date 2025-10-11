package com.lld.elevator.entities;

import com.lld.elevator.entities.button.Button;

import java.util.Map;

public class CabinControl {
    public int elevatorNumber;
    public Button doorOpen;
    public Button doorClose;
    public Button emergencyStop;
    public Button alarm;
    public Map<Integer, Button> floorButtons;

    public void pressFloor(int floor) {
        if (floorButtons != null && floorButtons.containsKey(floor)) {
            floorButtons.get(floor).press();
        }
    }
}
