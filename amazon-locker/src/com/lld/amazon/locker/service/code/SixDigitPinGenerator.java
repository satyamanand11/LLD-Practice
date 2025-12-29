package com.lld.amazon.locker.service.code;

import java.security.SecureRandom;

public class SixDigitPinGenerator implements CodeGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate6DigitPin() {
        int n = 100000 + random.nextInt(900000);
        return String.valueOf(n);
    }
}
