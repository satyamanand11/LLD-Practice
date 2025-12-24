package com.lld.hotel.management.service.payment;

import com.lld.hotel.management.entities.Booking;
import com.lld.hotel.management.entities.IdGenerator;
import com.lld.hotel.management.entities.Payment;
import com.lld.hotel.management.repository.PaymentRepository;

import java.math.BigDecimal;

public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final AbstractPaymentProcessor paymentProcessor;

    public PaymentService(
            PaymentRepository paymentRepo,
            AbstractPaymentProcessor paymentProcessor) {

        this.paymentRepo = paymentRepo;
        this.paymentProcessor = paymentProcessor;
    }

    public void captureAdvance(Booking booking, BigDecimal amount) {

        Payment payment = new Payment(IdGenerator.nextId(),
                booking.getBookingId(),
                amount
        );

        paymentProcessor.processPayment(amount);
        payment.capture();

        paymentRepo.save(payment);
        booking.markPaymentCompleted();
    }
}
