package com.lld.hotel.management.observer;

import com.lld.hotel.management.repository.AccountRepository;

import java.util.Optional;

/**
 * Notification Handler demonstrating Observer Pattern
 * Handles booking-related events and sends notifications
 */
public class NotificationHandler implements EventHandler<BookingConfirmedEvent> {

    private final AccountRepository accountRepository;

    public NotificationHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Class<BookingConfirmedEvent> eventType() {
        return BookingConfirmedEvent.class;
    }

    @Override
    public void handle(BookingConfirmedEvent event) {
        Optional<com.lld.hotel.management.entities.Account> accountOpt =
                accountRepository.findById(event.getGuestAccountId());

        if (accountOpt.isPresent()) {
            com.lld.hotel.management.entities.Account account = accountOpt.get();
            System.out.println("\n📧 [NOTIFICATION] Booking Confirmed!");
            System.out.println("   Guest: " + account.getName() + " (" + account.getEmail() + ")");
            System.out.println("   Booking ID: " + event.getBookingId());
            System.out.println("   Confirmed at: " + event.occurredAt());
        }
    }
}

