package com.lld.hotel.management.entities;

import java.math.BigDecimal;

public class Payment {

    private final int paymentId;
    private final int bookingId;
    private final BigDecimal amount;

    private PaymentStatus status;

    public Payment(int paymentId, int bookingId, BigDecimal amount) {

        if (paymentId <= 0) {
            throw new IllegalArgumentException("paymentId must be positive");
        }
        if (bookingId <= 0) {
            throw new IllegalArgumentException("bookingId must be positive");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("payment amount must be positive");
        }

        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = PaymentStatus.CREATED;
    }

    public void authorize() {
        if (status != PaymentStatus.CREATED) {
            throw new IllegalStateException("Payment can only be authorized from CREATED state");
        }
        this.status = PaymentStatus.AUTHORIZED;
    }

    public void capture() {
        if (status != PaymentStatus.CREATED && status != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException("Payment can only be captured from CREATED or AUTHORIZED state");
        }
        this.status = PaymentStatus.CAPTURED;
    }

    public void fail() {
        if (status == PaymentStatus.CAPTURED || status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Cannot fail a captured or refunded payment"
            );
        }
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (status != PaymentStatus.CAPTURED) {
            throw new IllegalStateException(
                    "Only captured payments can be refunded"
            );
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public boolean isCaptured() {
        return status == PaymentStatus.CAPTURED;
    }
}
