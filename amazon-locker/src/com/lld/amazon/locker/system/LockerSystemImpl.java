package com.lld.amazon.locker.system;

import com.lld.amazon.locker.dto.DeliveryReceipt;
import com.lld.amazon.locker.dto.PackageRequest;
import com.lld.amazon.locker.dto.ReturnReceipt;
import com.lld.amazon.locker.model.AllocatedSlot;
import com.lld.amazon.locker.model.Assignment;
import com.lld.amazon.locker.model.LockerLocation;
import com.lld.amazon.locker.repository.AssignmentRepository;
import com.lld.amazon.locker.repository.LocationRepository;
import com.lld.amazon.locker.service.allocation.LockerAllocationService;
import com.lld.amazon.locker.service.code.CodeGenerator;
import com.lld.amazon.locker.service.expiry.ExpiryService;
import com.lld.amazon.locker.service.notification.NotificationService;
import com.lld.amazon.locker.service.refund.RefundService;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class LockerSystemImpl implements LockerSystem {

    private static final Duration HOLD_DURATION = Duration.ofDays(3);

    private final LocationRepository locationRepo;
    private final AssignmentRepository assignmentRepo;
    private final LockerAllocationService allocationService;
    private final CodeGenerator codeGenerator;
    private final NotificationService notificationService;
    private final RefundService refundService;
    private final ExpiryService expiryService;
    private final Clock clock;

    public LockerSystemImpl(LocationRepository locationRepo,
                            AssignmentRepository assignmentRepo,
                            LockerAllocationService allocationService,
                            CodeGenerator codeGenerator,
                            NotificationService notificationService,
                            RefundService refundService,
                            ExpiryService expiryService,
                            Clock clock) {

        this.locationRepo = locationRepo;
        this.assignmentRepo = assignmentRepo;
        this.allocationService = allocationService;
        this.codeGenerator = codeGenerator;
        this.notificationService = notificationService;
        this.refundService = refundService;
        this.expiryService = expiryService;
        this.clock = clock;
    }

    @Override
    public List<DeliveryReceipt> deliverOrderToLocation(String orderId,
                                                        String customerId,
                                                        String preferredLocationId,
                                                        List<PackageRequest> packages) {

        // R7: location operating hours
        LockerLocation loc = locationRepo.findById(preferredLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + preferredLocationId));

        if (!loc.getOperatingHours().isWithin(LocalDateTime.now(clock))) {
            throw new IllegalStateException("Location is closed right now");
        }

        List<DeliveryReceipt> receipts = new ArrayList<>();

        // Each package gets one compartment (R9). If upstream packed together, packages size is smaller count (R2)
        for (PackageRequest pkg : packages) {
            // R4: only if fits in some compartment; allocation enforces it
            AllocatedSlot slot = allocationService.allocate(preferredLocationId, pkg.dimensions())
                    .orElseThrow(() -> new IllegalStateException("No compartment available for package=" + pkg.packageId()));

            String pin = codeGenerator.generate6DigitPin();
            Instant expiresAt = Instant.now(clock).plus(HOLD_DURATION);

            Assignment assignment = Assignment.createDelivery(
                    orderId, customerId,
                    slot.locationId(), slot.lockerId(), slot.compartmentId(),
                    pin, expiresAt
            );

            assignmentRepo.save(assignment);

            // R5: notify customer with code
            notificationService.notifyCustomer(customerId,
                    "Delivered package=" + pkg.packageId() + " to location=" + preferredLocationId +
                            " locker=" + slot.lockerId() + " compartment=" + slot.compartmentId() +
                            " PIN=" + pin + " expiresAt=" + expiresAt);

            // R6/R8: TTL expiry
            expiryService.scheduleExpiry(assignment.getAssignmentId(), expiresAt);

            receipts.add(new DeliveryReceipt(
                    assignment.getAssignmentId(),
                    slot.lockerId(),
                    slot.compartmentId(),
                    pin,
                    expiresAt
            ));
        }

        return receipts;
    }

    @Override
    public void pickupDelivery(String assignmentId, String customerId, String customerPin) {
        Assignment a = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assignmentId"));

        if (!a.getCustomerId().equals(customerId)) throw new SecurityException("Customer mismatch");

        LockerLocation loc = locationRepo.findById(a.getLocationId()).orElseThrow();
        if (!loc.getOperatingHours().isWithin(LocalDateTime.now(clock))) {
            throw new IllegalStateException("Location is closed right now");
        }

        // R10: once collected, code invalidated (status changes)
        a.validateCustomerPickup(customerPin, clock);
        a.markCollected();
        assignmentRepo.save(a);

        allocationService.release(new AllocatedSlot(a.getLocationId(), a.getLockerId(), a.getCompartmentId()));

        notificationService.notifyCustomer(customerId,
                "Pickup successful. Code invalidated. assignmentId=" + assignmentId);
    }

    @Override
    public ReturnReceipt initiateReturnDropOff(String returnId,
                                               String customerId,
                                               String preferredLocationId,
                                               PackageRequest returnPackage) {

        LockerLocation loc = locationRepo.findById(preferredLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + preferredLocationId));

        if (!loc.getOperatingHours().isWithin(LocalDateTime.now(clock))) {
            throw new IllegalStateException("Location is closed right now");
        }

        AllocatedSlot slot = allocationService.allocate(preferredLocationId, returnPackage.dimensions())
                .orElseThrow(() -> new IllegalStateException("No compartment available for returnPackage=" + returnPackage.packageId()));

        String customerDropOffCode = codeGenerator.generate6DigitPin();  // R11
        String logisticsPickupCode = codeGenerator.generate6DigitPin();  // R12
        Instant expiresAt = Instant.now(clock).plus(HOLD_DURATION);

        Assignment assignment = Assignment.createReturn(
                returnId, customerId,
                slot.locationId(), slot.lockerId(), slot.compartmentId(),
                customerDropOffCode, logisticsPickupCode, expiresAt
        );

        assignmentRepo.save(assignment);

        notificationService.notifyCustomer(customerId,
                "Return drop-off ready at location=" + preferredLocationId +
                        " locker=" + slot.lockerId() + " compartment=" + slot.compartmentId() +
                        " PIN=" + customerDropOffCode + " expiresAt=" + expiresAt);

        notificationService.notifyLogistics(
                "Return awaiting pickup. assignmentId=" + assignment.getAssignmentId() +
                        " locker=" + slot.lockerId() + " compartment=" + slot.compartmentId() +
                        " logisticsCode=" + logisticsPickupCode);

        expiryService.scheduleExpiry(assignment.getAssignmentId(), expiresAt);

        return new ReturnReceipt(
                assignment.getAssignmentId(),
                slot.lockerId(),
                slot.compartmentId(),
                customerDropOffCode,
                "Use this code to open locker and drop the return. Logistics uses a different code.",
                expiresAt
        );
    }

    @Override
    public void pickupReturnByLogistics(String assignmentId, String logisticsCode) {
        Assignment a = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assignmentId"));

        LockerLocation loc = locationRepo.findById(a.getLocationId()).orElseThrow();
        if (!loc.getOperatingHours().isWithin(LocalDateTime.now(clock))) {
            throw new IllegalStateException("Location is closed right now");
        }

        a.validateLogisticsPickup(logisticsCode, clock);
        a.markCollected();
        assignmentRepo.save(a);

        allocationService.release(new AllocatedSlot(a.getLocationId(), a.getLockerId(), a.getCompartmentId()));

        // R12: notify customer return processed + apply refund policy
        refundService.applyReturnRefundPolicy(a.getCustomerId(), a.getOrderOrReturnId());
        notificationService.notifyCustomer(a.getCustomerId(),
                "Return picked up by logistics and is being processed. assignmentId=" + assignmentId);
    }
}
