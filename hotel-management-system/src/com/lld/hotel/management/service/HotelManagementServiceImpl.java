package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.*;
import com.lld.hotel.management.repository.AccountRepository;
import com.lld.hotel.management.repository.HotelRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public class HotelManagementServiceImpl implements HotelManagementService {

    private static volatile HotelManagementServiceImpl INSTANCE;

    // Repositories
    private final AccountRepository accountRepository;
    private final HotelRepository hotelRepository;

    // Application services (orchestrators)
    private final BookingService bookingService;

    private HotelManagementServiceImpl(
            AccountRepository accountRepository,
            HotelRepository hotelRepository,
            BookingService bookingService) {

        this.accountRepository = accountRepository;
        this.hotelRepository = hotelRepository;
        this.bookingService = bookingService;
    }

    /* ================= Singleton Bootstrap ================= */

    public static HotelManagementService init(
            AccountRepository accountRepository,
            HotelRepository hotelRepository,
            BookingService bookingService) {

        if (INSTANCE == null) {
            synchronized (HotelManagementServiceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HotelManagementServiceImpl(
                            accountRepository,
                            hotelRepository,
                            bookingService
                    );
                }
            }
        }
        return INSTANCE;
    }

    public static HotelManagementService getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "HotelManagementService not initialized");
        }
        return INSTANCE;
    }

    /* ================= Account APIs ================= */

    @Override
    public Account registerAccount(
            String name,
            String email,
            Set<Role> roles) {

        Account account = new Account(
                IdGenerator.nextId(),
                name,
                email,
                roles
        );

        accountRepository.save(account);
        return account;
    }

    @Override
    public Account login(String email) {

        Optional<Account> account =
                accountRepository.findByEmail(email);

        return account.orElseThrow(
                () -> new IllegalArgumentException("Invalid credentials")
        );
    }

    /* ================= Hotel / Branch APIs ================= */

    @Override
    public Hotel registerHotel(int hotelId, String name) {

        Hotel hotel = new Hotel(hotelId, name);
        hotelRepository.save(hotel);
        return hotel;
    }

    @Override
    public void addBranch(
            int hotelId,
            int branchId,
            String city,
            String address) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Hotel not found"));

        HotelBranch branch =
                new HotelBranch(branchId, hotelId, city, address);

        hotel.registerBranch(branch);
        hotelRepository.save(hotel);
    }

    /* ================= Booking APIs ================= */

    @Override
    public Booking bookRoom(
            int hotelId,
            int branchId,
            Account actor,
            int roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal advanceAmount) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Hotel not found"));

        // Routing / context validation
        hotel.getBranch(branchId);

        // Minimal authorization guard (optional but clean)
        if (!actor.hasRole(Role.GUEST)
                && !actor.hasRole(Role.RECEPTIONIST)) {
            throw new SecurityException("Not allowed to book room");
        }

        Booking booking = new Booking(
                IdGenerator.nextId(),
                actor.getAccountId(),
                roomId,
                checkIn,
                checkOut
        );

        return bookingService.createBooking(
                actor,
                booking,
                new DateRange(checkIn, checkOut),
                advanceAmount
        );
    }

    @Override
    public void cancelBooking(
            int hotelId,
            int branchId,
            Account actor,
            int bookingId,
            LocalDateTime cancelTime) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Hotel not found"));

        hotel.getBranch(branchId);

        bookingService.cancelBooking(
                bookingId,
                cancelTime
        );
    }
}