package com.lld.bms.service.selection;

public class PopcornCombo implements AddOn {
    private static final int PRICE = 250;

    @Override
    public String name() {
        return "Popcorn Combo";
    }

    @Override
    public int price() {
        return PRICE;
    }
}
