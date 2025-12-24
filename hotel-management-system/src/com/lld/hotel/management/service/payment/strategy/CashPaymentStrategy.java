package com.lld.hotel.management.service.payment.strategy;

import java.math.BigDecimal;

public class CashPaymentStrategy implements PaymentStrategy {
    public void pay(BigDecimal amount) {
        System.out.println("Cash payment: " + amount);
    }
}
