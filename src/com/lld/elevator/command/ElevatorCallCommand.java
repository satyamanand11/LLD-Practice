package com.lld.elevator.command;


import com.lld.elevator.dto.ElevatorCallDTO;
import com.lld.elevator.service.ElevatorService;

public class ElevatorCallCommand implements Command {
    private final ElevatorCallDTO dto;
    private final ElevatorService service;

    public ElevatorCallCommand(ElevatorCallDTO dto, ElevatorService service) {
        this.dto = dto; this.service = service;
    }

    @Override
    public void execute() {
        service.requestCarCall(dto.elevatorId(), dto.floor());
    }
}
