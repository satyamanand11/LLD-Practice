package com.lld.bms.service.selection;

public class LargeCola implements AddOn {
    private static final int PRICE = 120;

    @Override
    public String name() {
        return "Large Cola";
    }

    @Override
    public int price() {
        return PRICE;
    }
}
