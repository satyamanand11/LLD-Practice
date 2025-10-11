package com.lld.elevator.facade;


import com.lld.elevator.command.CommandBus;
import com.lld.elevator.command.HallCallCommand;
import com.lld.elevator.dto.HallCallDTO;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.service.ElevatorService;

public class ElevatorSystem {
    private static ElevatorSystem INSTANCE;
    private final ElevatorService service;
    private final CommandBus bus;

    private ElevatorSystem(ElevatorService s, CommandBus b) { this.service = s; this.bus = b; }

    public static synchronized ElevatorSystem init(ElevatorService s, CommandBus b) {
        if (INSTANCE == null) INSTANCE = new ElevatorSystem(s, b);
        return INSTANCE;
    }

    public static ElevatorSystem get() { return INSTANCE; }

    public void pressUpButton(int floor)  { bus.submit(new HallCallCommand(new HallCallDTO(floor, Direction.UP), service)); }
    public void pressDownButton(int floor){ bus.submit(new HallCallCommand(new HallCallDTO(floor, Direction.DOWN), service)); }

    // Optional helper for direct cabin calls without going through a button instance
    public void pressCabinFloorButton(int elevatorId, int floor) {
        service.requestCarCall(elevatorId, floor);
    }

    public void step()  { service.stepAll(); }
    public void print() { service.printStatus(); }
    public void shutdown() { bus.shutdown(); }
}
