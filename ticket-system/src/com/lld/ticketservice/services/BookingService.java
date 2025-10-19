package com.lld.ticketservice.services;


import com.lld.ticketservice.domain.booking.Booking;
import com.lld.ticketservice.domain.booking.BookingStatus;
import com.lld.ticketservice.domain.show.*;
import com.lld.ticketservice.locking.SeatLockProvider;
import com.lld.ticketservice.managers.booking.BookingManager;
import com.lld.ticketservice.managers.show.ShowManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BookingService {
    private final ShowManager shows;
    private final SeatLockProvider locks;
    private final BookingManager bookingMgr;
    private final AtomicInteger ids = new AtomicInteger(1);

    public BookingService(ShowManager shows, SeatLockProvider locks, BookingManager bookingMgr){
        this.shows=shows; this.locks=locks; this.bookingMgr=bookingMgr;
    }

    public Booking reserve(String userId, int showId, List<Integer> seats, long ttlMs){
        if(!locks.tryLock(showId, seats, userId, ttlMs))
            throw new IllegalStateException("Seats not available: " + seats);
        Booking b = new Booking(ids.getAndIncrement(), showId, userId, seats);
        bookingMgr.save(b); return b;
    }

    public void confirmSeats(int bookingId, String userId){
        Booking b = bookingMgr.get(bookingId);
        Show show = shows.getShow(b.getShowId());
        for(int s : b.getSeatNumbers()){
            ShowSeat seat = show.getSeats().get(s);
            if(!userId.equals(seat.getLockedByUser()))
                throw new IllegalStateException("Seat " + s + " not locked by " + userId);
            seat.setStatus(ShowSeatStatus.BOOKED);
            seat.setLockedByUser(null);
            seat.setLockExpiresAt(0);
        }
        bookingMgr.updateStatus(bookingId, BookingStatus.CONFIRMED.name());
    }

    public void cancel(int bookingId){
        Booking b = bookingMgr.get(bookingId);
        locks.release(b.getShowId(), b.getSeatNumbers(), b.getUserId());
        bookingMgr.updateStatus(bookingId, BookingStatus.CANCELLED.name());
    }

    public Booking get(int bookingId){ return bookingMgr.get(bookingId); }

    public String showState(int showId){
        Show show = shows.getShow(showId);
        return show.getSeats().values().stream()
                .map(s -> s.getSeatNumber()+":"+s.getStatus())
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
