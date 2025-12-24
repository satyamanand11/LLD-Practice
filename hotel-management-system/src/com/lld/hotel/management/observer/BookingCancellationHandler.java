package com.lld.hotel.management.observer;

import com.lld.hotel.management.repository.AccountRepository;
import com.lld.hotel.management.repository.BookingRepository;

import java.util.Optional;

/**
 * Handler for booking cancellation events
 * Demonstrates Observer Pattern for cancellation notifications
 */
public class BookingCancellationHandler implements EventHandler<BookingCancelledEvent> {

    private final AccountRepository accountRepository;
    private final BookingRepository bookingRepository;

    public BookingCancellationHandler(
            AccountRepository accountRepository,
            BookingRepository bookingRepository) {
        this.accountRepository = accountRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Class<BookingCancelledEvent> eventType() {
        return BookingCancelledEvent.class;
    }

    @Override
    public void handle(BookingCancelledEvent event) {
        bookingRepository.executeWithLock(
                event.getBookingId(),
                booking -> {
                    Optional<com.lld.hotel.management.entities.Account> accountOpt =
                            accountRepository.findById(booking.getGuestAccountId());

                    if (accountOpt.isPresent()) {
                        com.lld.hotel.management.entities.Account account = accountOpt.get();
                        System.out.println("\n📧 [NOTIFICATION] Booking Cancelled!");
                        System.out.println("   Guest: " + account.getName() + " (" + account.getEmail() + ")");
                        System.out.println("   Booking ID: " + event.getBookingId());
                        System.out.println("   Cancelled at: " + event.occurredAt());
                        
                        if (booking.isEligibleForFullRefund(event.occurredAt())) {
                            System.out.println("   ✅ Full refund eligible (cancelled >24hrs before check-in)");
                        } else {
                            System.out.println("   ⚠️  No refund (cancelled within 24hrs of check-in)");
                        }
                    }
                }
        );
    }
}

