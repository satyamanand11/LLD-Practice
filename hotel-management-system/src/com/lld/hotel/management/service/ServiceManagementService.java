package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.*;
import com.lld.hotel.management.repository.BookingRepository;
import com.lld.hotel.management.repository.BookingServiceRepository;
import com.lld.hotel.management.repository.ServiceRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * ServiceManagementService (R8)
 * Manages additional services for bookings
 */
public class ServiceManagementService {
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final BookingServiceRepository bookingServiceRepository;

    public ServiceManagementService(
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository,
            BookingServiceRepository bookingServiceRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.bookingServiceRepository = bookingServiceRepository;
    }

    public BookingServiceEntity addServiceToBooking(int bookingId, int serviceId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        // Validate booking exists
        bookingRepository.executeWithLock(bookingId, booking -> {
            if (booking.getStatus() == BookingStatus.CANCELLED || 
                booking.getStatus() == BookingStatus.CHECKED_OUT) {
                throw new IllegalStateException("Cannot add service to cancelled or checked-out booking");
            }
        });

        // Validate service exists
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        BigDecimal totalPrice = service.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        BookingServiceEntity bookingService = new BookingServiceEntity(
                IdGenerator.nextId(),
                bookingId,
                serviceId,
                quantity,
                totalPrice
        );

        bookingService.activate();
        bookingServiceRepository.save(bookingService);

        return bookingService;
    }

    public void removeServiceFromBooking(int bookingServiceId) {
        BookingServiceEntity bookingService = bookingServiceRepository.findById(bookingServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Booking service not found"));

        if (bookingService.getStatus() == BookingServiceEntity.BookingServiceStatus.COMPLETED) {
            throw new IllegalStateException("Cannot remove completed service");
        }

        bookingService.cancel();
        bookingServiceRepository.save(bookingService);
    }

    public List<Service> getAvailableServices() {
        return serviceRepository.findAll();
    }

    public List<Service> getServicesByType(Service.ServiceType serviceType) {
        return serviceRepository.findByType(serviceType);
    }

    public List<BookingServiceEntity> getBookingServices(int bookingId) {
        return bookingServiceRepository.findByBookingId(bookingId);
    }
}

