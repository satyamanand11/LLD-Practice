package com.demo.dto.parkingSpot;

import com.demo.interfaces.ParkingSpotState;
import com.demo.states.FreeState;
import com.demo.states.OccupiedState;

import java.util.concurrent.atomic.*;

public abstract class ParkingSpot {
    private static final AtomicInteger x = new AtomicInteger(0);
    private int id;
    private ParkingSpotState state;
    private int floor;
    protected int amount;

    public ParkingSpot(){}

    public ParkingSpot(int floor, int amount) {
        this.floor = floor;
        this.amount = amount;
        this.state = new FreeState();
        id= x.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFree() {
        return state.isAvailable();
    }

    public void setFree(boolean free) {
        if (free) {
            this.state = new FreeState();
        } else {
            this.state = new OccupiedState();
        }
    }

    public ParkingSpotState getState() {
        return state;
    }

    public void setState(ParkingSpotState state) {
        this.state = state;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    public abstract int cost(int parkingHours);
}
