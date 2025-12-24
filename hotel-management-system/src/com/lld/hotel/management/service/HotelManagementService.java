package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.Set;

public interface HotelManagementService {

    Account registerAccount(
            String name,
            String email,
            Set<Role> roles
    );

    Account login(String email);


    Hotel registerHotel(
            int hotelId,
            String name
    );

    void addBranch(
            int hotelId,
            int branchId,
            String city,
            String address
    );


    /* ================= Booking APIs ================= */

    Booking bookRoom(
            int hotelId,
            int branchId,
            Account actor,
            int roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal advanceAmount
    );

    void cancelBooking(
            int hotelId,
            int branchId,
            Account actor,
            int bookingId,
            LocalDateTime cancelTime
    );
}