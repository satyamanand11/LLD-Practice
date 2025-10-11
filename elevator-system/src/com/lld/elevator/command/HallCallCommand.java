package com.lld.elevator.command;


import com.lld.elevator.dto.HallCallDTO;
import com.lld.elevator.service.ElevatorService;

public class HallCallCommand implements Command {
    private final HallCallDTO dto;
    private final ElevatorService service;

    public HallCallCommand(HallCallDTO dto, ElevatorService service) {
        this.dto = dto; this.service = service;
    }

    @Override public void execute() {
        service.requestElevator(dto.floor(), dto.direction());
    }
}
