package com.lld.bms.service.selection;

public class RecliningUpgrade implements AddOn {
    private static final int PRICE = 180;

    @Override
    public String name() {
        return "Reclining Upgrade";
    }

    @Override
    public int price() {
        return PRICE;
    }
}
