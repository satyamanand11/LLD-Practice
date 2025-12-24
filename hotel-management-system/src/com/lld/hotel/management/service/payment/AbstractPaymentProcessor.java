package com.lld.hotel.management.service.payment;

import java.math.BigDecimal;

public abstract class AbstractPaymentProcessor {

    public final void processPayment(BigDecimal amount) {
        validate(amount);
        doPayment(amount);
        afterSuccess();
    }

    protected void validate(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    protected abstract void doPayment(BigDecimal amount);

    protected void afterSuccess() {
        // logging / metrics hook
    }
}
