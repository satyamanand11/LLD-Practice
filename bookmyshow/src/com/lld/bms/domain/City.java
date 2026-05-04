package com.lld.bms.domain;

public class City {
    private final String id;
    private final String name;
    private final String state;

    public City(String id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }
}
