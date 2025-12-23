//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.lld.amazon.locker.dto.DeliveryReceipt;
import com.lld.amazon.locker.dto.PackageRequest;
import com.lld.amazon.locker.dto.ReturnReceipt;
import com.lld.amazon.locker.model.*;
import com.lld.amazon.locker.repository.*;
import com.lld.amazon.locker.scheduler.DefaultScheduler;
import com.lld.amazon.locker.scheduler.Scheduler;
import com.lld.amazon.locker.service.allocation.DefaultLockerAllocationService;
import com.lld.amazon.locker.service.allocation.LockerAllocationService;
import com.lld.amazon.locker.service.code.CodeGenerator;
import com.lld.amazon.locker.service.code.SixDigitPinGenerator;
import com.lld.amazon.locker.service.expiry.DefaultExpiryService;
import com.lld.amazon.locker.service.expiry.ExpiryService;
import com.lld.amazon.locker.service.notification.ConsoleNotificationService;
import com.lld.amazon.locker.service.notification.NotificationService;
import com.lld.amazon.locker.service.refund.ConsoleRefundService;
import com.lld.amazon.locker.service.refund.RefundService;
import com.lld.amazon.locker.system.LockerSystem;
import com.lld.amazon.locker.system.LockerSystemImpl;

import java.time.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        Clock clock = Clock.systemDefaultZone();

        // Repos
        LockerRepository lockerRepo = new InMemoryLockerRepository();
        LocationRepository locationRepo = new InMemoryLocationRepository();
        AssignmentRepository assignmentRepo = new InMemoryAssignmentRepository();

        // Infra services
        CodeGenerator codeGen = new SixDigitPinGenerator();
        NotificationService notifier = new ConsoleNotificationService();
        RefundService refundService = new ConsoleRefundService();
        Scheduler scheduler = new DefaultScheduler();

        // Domain/app services
        LockerAllocationService allocationService = new DefaultLockerAllocationService(locationRepo, lockerRepo);
        ExpiryService expiryService = new DefaultExpiryService(
                assignmentRepo, allocationService, refundService, notifier, scheduler, clock
        );

        // Facade
        LockerSystem lockerSystem = new LockerSystemImpl(
                locationRepo, assignmentRepo, allocationService, codeGen, notifier, refundService, expiryService, clock
        );

        // Seed data: 1 location with 2 lockers, each locker has compartments of different sizes
        String locationId = "LOC-BLR-001";
        OperatingHours hours = new OperatingHours(LocalTime.of(9, 0), LocalTime.of(21, 0));

        String locker1 = "LOCKER-1";
        String locker2 = "LOCKER-2";

        Locker l1 = new Locker(locker1, List.of(
                new Compartment("C1", locker1, LockerSize.S, new Dimensions(20,20,20)),
                new Compartment("C2", locker1, LockerSize.M, new Dimensions(35,35,35))
        ));

        Locker l2 = new Locker(locker2, List.of(
                new Compartment("C3", locker2, LockerSize.M, new Dimensions(35,35,35)),
                new Compartment("C4", locker2, LockerSize.L, new Dimensions(50,50,50))
        ));

        lockerRepo.save(l1);
        lockerRepo.save(l2);

        LockerLocation loc = new LockerLocation(locationId, "Bangalore Indiranagar", hours, List.of(locker1, locker2));
        locationRepo.save(loc);

        // --- DELIVERY (R1-R10) ---
        String orderId = "ORDER-123";
        String customerId = "CUST-7";

        List<PackageRequest> packages = List.of(
                new PackageRequest("PKG-1", new Dimensions(10,10,10)),
                new PackageRequest("PKG-2", new Dimensions(30,30,30))
        );

        System.out.println("\n=== Delivering order to preferred location ===");
        List<DeliveryReceipt> receipts = lockerSystem.deliverOrderToLocation(orderId, customerId, locationId, packages);

        // Pickup the first package
        DeliveryReceipt first = receipts.get(0);
        System.out.println("\n=== Customer pickup ===");
        lockerSystem.pickupDelivery(first.assignmentId(), customerId, first.customerCode());

        // --- RETURN (R11-R12) ---
        System.out.println("\n=== Initiate return drop-off ===");
        ReturnReceipt returnReceipt = lockerSystem.initiateReturnDropOff(
                "RETURN-9", customerId, locationId,
                new PackageRequest("RET-PKG-1", new Dimensions(10,10,10))
        );

        System.out.println("\n=== Logistics pickup return ===");
        // In real system, logistics code comes from secure channel; for demo, we read it from repository:
        Assignment returnAssignment = assignmentRepo.findById(returnReceipt.assignmentId()).orElseThrow();
        lockerSystem.pickupReturnByLogistics(returnReceipt.assignmentId(), returnAssignment.getLogisticsCode());

        // Let scheduler thread run (for demo only)
        Thread.sleep(200);

        scheduler.shutdown();
        System.out.println("\n=== Demo complete ===");
    }
}