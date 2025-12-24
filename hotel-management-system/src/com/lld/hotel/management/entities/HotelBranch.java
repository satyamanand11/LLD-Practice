package com.lld.hotel.management.entities;

public record HotelBranch(int branchId, int hotelId, String city, String address) {

    public HotelBranch {

        if (branchId <= 0) {
            throw new IllegalArgumentException("branchId must be positive");
        }
        if (hotelId <= 0) {
            throw new IllegalArgumentException("hotelId must be positive");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city is required");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address is required");
        }

    }
}
