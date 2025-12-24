package com.lld.hotel.management.service.payment.strategy;

import com.lld.hotel.management.service.payment.AbstractPaymentProcessor;

import java.math.BigDecimal;

public class StrategyPaymentProcessor extends AbstractPaymentProcessor {

    private final PaymentStrategy strategy;

    public StrategyPaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    protected void doPayment(BigDecimal amount) {
        strategy.pay(amount);
    }
}
