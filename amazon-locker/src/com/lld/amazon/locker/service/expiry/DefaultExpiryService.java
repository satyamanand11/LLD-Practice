package com.lld.amazon.locker.service.expiry;

import com.lld.amazon.locker.model.AllocatedSlot;
import com.lld.amazon.locker.model.Assignment;
import com.lld.amazon.locker.model.AssignmentType;
import com.lld.amazon.locker.repository.AssignmentRepository;
import com.lld.amazon.locker.scheduler.Scheduler;
import com.lld.amazon.locker.service.allocation.LockerAllocationService;
import com.lld.amazon.locker.service.notification.NotificationService;
import com.lld.amazon.locker.service.refund.RefundService;

import java.time.*;
import java.util.Optional;

public class DefaultExpiryService implements ExpiryService {

    private final AssignmentRepository assignmentRepo;
    private final LockerAllocationService allocationService;
    private final RefundService refundService;
    private final NotificationService notificationService;
    private final Scheduler scheduler;
    private final Clock clock;

    public DefaultExpiryService(AssignmentRepository assignmentRepo,
                                LockerAllocationService allocationService,
                                RefundService refundService,
                                NotificationService notificationService,
                                Scheduler scheduler,
                                Clock clock) {
        this.assignmentRepo = assignmentRepo;
        this.allocationService = allocationService;
        this.refundService = refundService;
        this.notificationService = notificationService;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    @Override
    public void scheduleExpiry(String assignmentId, Instant expiresAt) {
        Duration delay = Duration.between(Instant.now(clock), expiresAt);
        scheduler.schedule(() -> expireIfNeeded(assignmentId), delay);
    }

    private void expireIfNeeded(String assignmentId) {
        Optional<Assignment> opt = assignmentRepo.findById(assignmentId);
        if (opt.isEmpty()) return;

        Assignment a = opt.get();
        boolean expired = a.tryExpire(clock);
        if (!expired) return;

        // Release compartment (R8)
        allocationService.release(new AllocatedSlot(a.getLocationId(), a.getLockerId(), a.getCompartmentId()));
        assignmentRepo.save(a);

        // Refund (R8) - for deliveries; returns may have different policy
        if (a.getType() == AssignmentType.DELIVERY) {
            refundService.refundCustomer(a.getCustomerId(), a.getOrderOrReturnId(), "Not picked within 3 days");
            notificationService.notifyCustomer(a.getCustomerId(),
                    "Package expired; compartment released; refund initiated. assignmentId=" + a.getAssignmentId());
        } else {
            notificationService.notifyCustomer(a.getCustomerId(),
                    "Return window expired; compartment released. assignmentId=" + a.getAssignmentId());
        }
    }
}
