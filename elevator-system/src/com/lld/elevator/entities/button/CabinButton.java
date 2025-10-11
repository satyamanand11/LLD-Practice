package com.lld.elevator.entities.button;


import com.lld.elevator.command.CommandBus;
import com.lld.elevator.command.ElevatorCallCommand;
import com.lld.elevator.dto.ElevatorCallDTO;
import com.lld.elevator.service.ElevatorService;

public class CabinButton extends Button {
    private final int elevatorId;
    private final int floor;
    private final CommandBus bus;
    private final ElevatorService service;

    public CabinButton(int elevatorId, int floor, CommandBus bus, ElevatorService service) {
        this.elevatorId = elevatorId;
        this.floor = floor;
        this.bus = bus;
        this.service = service;
    }

    @Override
    public void press() {
        light();
        bus.submit(new ElevatorCallCommand(new ElevatorCallDTO(elevatorId, floor), service));
    }
}
