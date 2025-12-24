package com.lld.hotel.management.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Hotel {

    private final int hotelId;
    private final String name;

    // Branch registry (owned by Hotel)
    private final Map<Integer, HotelBranch> branches = new HashMap<>();

    public Hotel(int hotelId, String name) {
        if (hotelId <= 0) {
            throw new IllegalArgumentException("hotelId must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("hotel name is required");
        }
        this.hotelId = hotelId;
        this.name = name;
    }

    /* ===============================
       Invariants protected by Hotel
       =============================== */

    /**
     * Invariant:
     * - Branch IDs must be unique within a hotel
     */
    public void registerBranch(HotelBranch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("branch cannot be null");
        }
        if (branches.containsKey(branch.branchId())) {
            throw new IllegalStateException(
                    "Branch already exists with id: " + branch.branchId()
            );
        }
        branches.put(branch.branchId(), branch);
    }

    /**
     * Invariant:
     * - A branch must exist to be used
     */
    public HotelBranch getBranch(int branchId) {
        HotelBranch branch = branches.get(branchId);
        if (branch == null) {
            throw new IllegalArgumentException(
                    "No branch found with id: " + branchId
            );
        }
        return branch;
    }

    /**
     * Read-only access to branches
     */
    public Collection<HotelBranch> getAllBranches() {
        return Collections.unmodifiableCollection(branches.values());
    }

    /* ===============================
       Simple getters
       =============================== */

    public int getHotelId() {
        return hotelId;
    }

    public String getName() {
        return name;
    }
}
