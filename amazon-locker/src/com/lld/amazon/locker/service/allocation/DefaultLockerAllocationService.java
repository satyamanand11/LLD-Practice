package com.lld.amazon.locker.service.allocation;

import com.lld.amazon.locker.model.*;
import com.lld.amazon.locker.repository.LocationRepository;
import com.lld.amazon.locker.repository.LockerRepository;

import java.util.Optional;

public class DefaultLockerAllocationService implements LockerAllocationService {

    private final LocationRepository locationRepo;
    private final LockerRepository lockerRepo;

    public DefaultLockerAllocationService(LocationRepository locationRepo, LockerRepository lockerRepo) {
        this.locationRepo = locationRepo;
        this.lockerRepo = lockerRepo;
    }

    @Override
    public Optional<AllocatedSlot> allocate(String locationId, Dimensions required) {
        LockerLocation location = locationRepo.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));

        // Try lockers in this location until one reserves successfully
        for (String lockerId : location.getLockerIds()) {
            Locker locker = lockerRepo.findById(lockerId)
                    .orElseThrow(() -> new IllegalStateException("Locker missing: " + lockerId));

            Optional<Compartment> reserved = locker.reserveSmallestFit(required);
            if (reserved.isPresent()) {
                return Optional.of(new AllocatedSlot(locationId, lockerId, reserved.get().getCompartmentId()));
            }
        }
        return Optional.empty();
    }

    @Override
    public void release(AllocatedSlot slot) {
        Locker locker = lockerRepo.findById(slot.lockerId()).orElseThrow();
        locker.releaseCompartment(slot.compartmentId());
    }
}
