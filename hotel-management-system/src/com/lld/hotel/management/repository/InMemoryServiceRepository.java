package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryServiceRepository implements ServiceRepository {
    private final Map<Integer, Service> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Service> findById(int serviceId) {
        return Optional.ofNullable(store.get(serviceId));
    }

    @Override
    public List<Service> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Service> findByType(Service.ServiceType serviceType) {
        return store.values().stream()
                .filter(service -> service.getServiceType() == serviceType)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Service service) {
        store.put(service.getServiceId(), service);
    }
}

