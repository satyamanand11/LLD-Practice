package com.demo.services;

import com.demo.dto.*;
import com.demo.dto.parkingSpot.*;
import com.demo.dto.parkingSpot.spotDecorator.*;
import com.demo.dto.vehicle.*;
import com.demo.enums.*;
import com.demo.exceptions.*;
import com.demo.interfaces.*;
import com.demo.interfaces.Observer;
import com.demo.parkingStrategy.*;

import java.util.*;

public class ParkingServiceImpl implements ParkingService {

    private final Strategy parkingStrategy;
    private final ParkingLot parkingLot;
    private final DisplayService displayService;
    private final ParkingSpotRepository repository;
    private final List<Observer> observers;

    public ParkingServiceImpl(Strategy parkingStrategy, DisplayService displayService, ParkingSpotRepository repository) {
        this.parkingStrategy = parkingStrategy;
        this.displayService = displayService;
        this.repository = repository;
        this.parkingLot = ParkingLot.getInstance();
        this.observers = new ArrayList<>();
    }

    @Override
    public ParkingTicket entry(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        
        ParkingSpotEnum parkingSpotEnum = vehicle.getParkingSpotEnum();
        List<ParkingSpot> freeParkingSpots = parkingLot.getFreeParkingSpots().get(parkingSpotEnum);
        List<ParkingSpot> occupiedParkingSpots = parkingLot.getOccupiedParkingSpots().get(parkingSpotEnum);

        try {
            ParkingSpot parkingSpot = parkingStrategy.findParkingSpot(parkingSpotEnum);
            
            if (parkingSpot == null) {
                throw new SpotNotFoundException("No parking spot available for " + parkingSpotEnum);
            }

            // Use double-checked locking for thread safety
            if (parkingSpot.isFree()) {
                synchronized (parkingSpot) {
                    if (parkingSpot.isFree()) {
                        parkingSpot.setFree(false);
                        freeParkingSpots.remove(parkingSpot);
                        occupiedParkingSpots.add(parkingSpot);
                        ParkingTicket parkingTicket = new ParkingTicket(vehicle, parkingSpot);

                        ParkingEvent parkingEvent = new ParkingEvent(ParkingEventType.EnTRY, parkingSpotEnum);
                        notifyAllObservers(parkingEvent);
                        return parkingTicket;
                    }
                }
            }
            
            // If spot is no longer free, try to find another spot
            return entry(vehicle);

        } catch (SpotNotFoundException e) {
            throw new RuntimeException("No parking spots available for " + parkingSpotEnum, e);
        } catch (Exception e) {
            throw new RuntimeException("Error during parking entry", e);
        }
    }

    public void addObserver(Observer observer){
        observers.add(observer);
    }

    public void notifyAllObservers(ParkingEvent parkingEvent){
        for(Observer observer: observers){
            observer.update(parkingEvent);
        }
    }

    private void addParkingSpotInFreeList( List<ParkingSpot> parkingSpots, ParkingSpot parkingSpot){
        parkingSpots.add(parkingSpot);
    }

    public  void addWash(ParkingTicket parkingTicket){
        parkingTicket.setParkingSpot( new Wash( parkingTicket.getParkingSpot()));
        return;
    }

    @Override
    public int exit(ParkingTicket parkingTicket, Vehicle vehicle) throws InvalidTicketException {
        if(parkingTicket.getVehicle().equals(vehicle)){
            ParkingSpot parkingSpot= parkingTicket.getParkingSpot();
            int amount= parkingSpot.getAmount();
            parkingSpot.setFree(true);
            parkingLot.getOccupiedParkingSpots().get(vehicle.getParkingSpotEnum()).remove(parkingSpot);
            addParkingSpotInFreeList(parkingLot.getFreeParkingSpots().get(vehicle.getParkingSpotEnum()) , parkingSpot);

            ParkingEvent parkingEvent= new ParkingEvent(ParkingEventType.EXIT, vehicle.getParkingSpotEnum());
            notifyAllObservers(parkingEvent);
            return amount;
        }
        else {
            throw  new InvalidTicketException("This is an invalid ticket");
        }

    }
}
