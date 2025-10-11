package com.lld.elevator.entities.button;


import com.lld.elevator.command.CommandBus;
import com.lld.elevator.command.HallCallCommand;
import com.lld.elevator.dto.HallCallDTO;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.service.ElevatorService;

public class HallButton extends Button {
    private final int floor;
    private final Direction direction;
    private final CommandBus bus;
    private final ElevatorService service;

    public HallButton(int floor, Direction direction, CommandBus bus, ElevatorService service) {
        this.floor = floor; this.direction = direction; this.bus = bus; this.service = service;
    }

    @Override
    public void press() {
        light();
        bus.submit(new HallCallCommand(new HallCallDTO(floor, direction), service));
    }
}
