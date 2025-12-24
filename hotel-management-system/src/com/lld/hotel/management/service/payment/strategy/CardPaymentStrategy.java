package com.lld.hotel.management.service.payment.strategy;

import java.math.BigDecimal;

public class CardPaymentStrategy implements PaymentStrategy {
    public void pay(BigDecimal amount) {
        System.out.println("Card payment: " + amount);
    }
}
