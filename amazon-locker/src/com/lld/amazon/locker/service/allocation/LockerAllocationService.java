package com.lld.amazon.locker.service.allocation;

import com.lld.amazon.locker.model.AllocatedSlot;
import com.lld.amazon.locker.model.Dimensions;

import java.util.Optional;

public interface LockerAllocationService {
    Optional<AllocatedSlot> allocate(String locationId, Dimensions required);
    void release(AllocatedSlot slot);
}
