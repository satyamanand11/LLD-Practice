package com.lld.hotel.management.entities;

import java.math.BigDecimal;

/**
 * Service Entity (R8)
 * Represents additional services that can be added to bookings
 */
public class Service {
    public enum ServiceType {
        ROOM_SERVICE,
        FOOD_SERVICE,
        KITCHEN_SERVICE,
        AMENITY_SERVICE
    }

    private final int serviceId;
    private final ServiceType serviceType;
    private final String name;
    private final String description;
    private final BigDecimal basePrice;

    public Service(int serviceId, ServiceType serviceType, String name, 
                   String description, BigDecimal basePrice) {
        if (serviceId <= 0) {
            throw new IllegalArgumentException("serviceId must be positive");
        }
        if (serviceType == null) {
            throw new IllegalArgumentException("serviceType is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("basePrice must be non-negative");
        }

        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }

    public int getServiceId() {
        return serviceId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}

