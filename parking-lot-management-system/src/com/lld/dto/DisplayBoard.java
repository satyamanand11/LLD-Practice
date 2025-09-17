package com.demo.dto;

import com.demo.enums.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayBoard {

    private static volatile DisplayBoard displayBoard = null;
    private static final Object lock = new Object();
    private Map<ParkingSpotEnum, AtomicInteger> freeParkingSpots;

    private DisplayBoard() {
        this.freeParkingSpots = new ConcurrentHashMap<>();
    }

    public static DisplayBoard getInstance(){
        if(displayBoard == null){
            synchronized (lock) {
                if(displayBoard == null){
                    displayBoard = new DisplayBoard();
                }
            }
        }
        return displayBoard;
    }

    public Map<ParkingSpotEnum, Integer> getFreeParkingSpots() {
        Map<ParkingSpotEnum, Integer> result = new HashMap<>();
        freeParkingSpots.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }

    public void updateFreeSpots(ParkingSpotEnum spotType, int change) {
        freeParkingSpots.computeIfAbsent(spotType, k -> new AtomicInteger(0))
                        .addAndGet(change);
    }

    public int getFreeSpotsCount(ParkingSpotEnum spotType) {
        return freeParkingSpots.getOrDefault(spotType, new AtomicInteger(0)).get();
    }

}
