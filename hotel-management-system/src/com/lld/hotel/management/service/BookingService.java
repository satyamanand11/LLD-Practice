package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.*;
import com.lld.hotel.management.observer.BookingCancelledEvent;
import com.lld.hotel.management.observer.BookingConfirmedEvent;
import com.lld.hotel.management.observer.EventBus;
import com.lld.hotel.management.repository.BookingRepository;
import com.lld.hotel.management.repository.RoomAvailabilityRepository;
import com.lld.hotel.management.service.payment.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomAvailabilityRepository availabilityRepo;
    private final PaymentService paymentService;
    private final EventBus eventBus;

    public BookingService(
            BookingRepository bookingRepo,
            RoomAvailabilityRepository availabilityRepo,
            PaymentService paymentService,
            EventBus eventBus) {

        this.bookingRepo = bookingRepo;
        this.availabilityRepo = availabilityRepo;
        this.paymentService = paymentService;
        this.eventBus = eventBus;
    }

    public Booking createBooking(
            Account actor,
            Booking booking,
            DateRange range,
            BigDecimal advanceAmount) {

        availabilityRepo.executeWithLock(
                booking.getRoomId(),
                availability -> availability.reserve(range)
        );

        paymentService.captureAdvance(booking, advanceAmount);

        booking.confirm();
        bookingRepo.create(booking);

        eventBus.publish(
                new BookingConfirmedEvent(
                        booking.getBookingId(),
                        booking.getGuestAccountId()
                )
        );

        return booking;
    }

    public void cancelBooking(
            int bookingId,
            LocalDateTime cancelTime) {

        bookingRepo.executeWithLock(
                bookingId,
                booking -> booking.cancel(cancelTime)
        );

        eventBus.publish(
                new BookingCancelledEvent(bookingId)
        );
    }
}