package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository {
    Optional<Service> findById(int serviceId);
    List<Service> findAll();
    List<Service> findByType(Service.ServiceType serviceType);
    void save(Service service);
}

