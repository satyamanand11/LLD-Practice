
import com.lld.elevator.entities.Elevator;
import com.lld.elevator.enums.EventType;
import com.lld.elevator.events.DomainEventBus;
import com.lld.elevator.facade.ElevatorSystem;
import com.lld.elevator.factory.CommandBusFactory;
import com.lld.elevator.factory.ElevatorFactory;
import com.lld.elevator.factory.ElevatorServiceFactory;
import com.lld.elevator.factory.StrategyFactory;
import com.lld.elevator.manager.ElevatorManager;
import com.lld.elevator.observer.PanelObserver;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        // Event bus + observers
        DomainEventBus bus = new DomainEventBus();
        bus.subscribe(EventType.HALL_CALL, new PanelObserver());
        bus.subscribe(EventType.ASSIGNMENT, new PanelObserver());
        bus.subscribe(EventType.SERVED, new PanelObserver());

        // Build elevators + manager
        List<Elevator> elevators = ElevatorFactory.create(2, bus, 0);
        ElevatorManager manager = new ElevatorManager(elevators);

        // ===== Demo 1: Broadcast strategy (single-thread command bus) =====
        System.out.println("=== DEMO 1: Broadcast Strategy (single-thread) ===");
        var svc1 = ElevatorServiceFactory.create(manager, StrategyFactory.broadcast(), bus);
        var bus1 = CommandBusFactory.async(100, 1);
        ElevatorSystem.init(svc1, bus1);
        var sys1 = ElevatorSystem.get();

        sys1.pressUpButton(3);
        Thread.sleep(200);
        for (int i = 0; i < 6; i++) { sys1.step(); sys1.print(); Thread.sleep(300); }
        sys1.shutdown();

        // ===== Demo 2: Exclusive ETA (multi-thread bus) + a Cabin call =====
        System.out.println("\n=== DEMO 2: Exclusive ETA Strategy (multi-thread) ===");
        var svc2 = ElevatorServiceFactory.create(manager, StrategyFactory.exclusiveEta(), bus);
        var bus2 = CommandBusFactory.async(100, 4);
        ElevatorSystem.init(svc2, bus2);
        var sys2 = ElevatorSystem.get();

        ExecutorService pool = Executors.newFixedThreadPool(3);
        pool.submit(() -> sys2.pressUpButton(2));   // hall call
        pool.submit(() -> sys2.pressUpButton(7));   // hall call
        pool.submit(() -> sys2.pressDownButton(10));// hall call
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);

        // Cabin call: passenger inside Elevator #1 presses floor 8
        sys2.pressCabinFloorButton(1, 8);

        for (int i = 0; i < 6; i++) { sys2.step(); sys2.print(); Thread.sleep(300); }
        sys2.shutdown();
    }
}