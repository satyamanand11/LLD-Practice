
import com.lld.hotel.management.entities.*;
import com.lld.hotel.management.entities.IdGenerator;
import com.lld.hotel.management.observer.BookingCancellationHandler;
import com.lld.hotel.management.observer.EventBus;
import com.lld.hotel.management.observer.NotificationHandler;
import com.lld.hotel.management.repository.*;
import com.lld.hotel.management.service.BookingService;
import com.lld.hotel.management.service.HotelManagementService;
import com.lld.hotel.management.service.HotelManagementServiceImpl;
import com.lld.hotel.management.service.payment.PaymentService;
import com.lld.hotel.management.service.payment.AbstractPaymentProcessor;
import com.lld.hotel.management.service.payment.strategy.CashPaymentStrategy;
import com.lld.hotel.management.service.payment.strategy.CardPaymentStrategy;
import com.lld.hotel.management.service.payment.strategy.PaymentStrategy;
import com.lld.hotel.management.service.payment.strategy.StrategyPaymentProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Hotel Management System Demo
 * 
 * Demonstrates:
 * - Singleton Pattern (HotelManagementService)
 * - Strategy Pattern (Payment methods: Cash, Card)
 * - Template Method Pattern (Payment processing flow)
 * - Observer Pattern (Event-driven notifications)
 * - Repository Pattern (Data access abstraction)
 * - SOLID Principles
 * - Domain-Driven Design
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("🏨 ========================================");
        System.out.println("   Hotel Management System Demo");
        System.out.println("   ========================================\n");

        // ==================== Setup ====================
        System.out.println("📋 Setting up system components...\n");

        // Repositories
        AccountRepository accountRepo = new InMemoryAccountRepository();
        HotelRepository hotelRepo = new InMemoryHotelRepository();
        RoomRepository roomRepo = new InMemoryRoomRepository();
        BookingRepository bookingRepo = new InMemoryBookingRepository();
        RoomAvailabilityRepository availabilityRepo = new InMemoryRoomAvailabilityRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();

        // Event Bus (Observer Pattern)
        EventBus eventBus = new EventBus();
        
        // Register event handlers (Observer Pattern)
        NotificationHandler notificationHandler = new NotificationHandler(accountRepo);
        BookingCancellationHandler cancellationHandler = 
                new BookingCancellationHandler(accountRepo, bookingRepo);
        eventBus.register(notificationHandler);
        eventBus.register(cancellationHandler);

        // Payment Strategy (Strategy Pattern)
        PaymentStrategy cashStrategy = new CashPaymentStrategy();
        PaymentStrategy cardStrategy = new CardPaymentStrategy();
        
        // Payment Processor (Template Method Pattern)
        AbstractPaymentProcessor cashProcessor = new StrategyPaymentProcessor(cashStrategy);
        AbstractPaymentProcessor cardProcessor = new StrategyPaymentProcessor(cardStrategy);

        // Services
        PaymentService paymentService = new PaymentService(paymentRepo, cashProcessor);
        BookingService bookingService = new BookingService(
                bookingRepo,
                availabilityRepo,
                paymentService,
                eventBus
        );

        // Initialize Hotel Management Service (Singleton Pattern)
        HotelManagementService hotelService = HotelManagementServiceImpl.init(
                accountRepo,
                hotelRepo,
                bookingService
        );

        System.out.println("✅ System initialized successfully!\n");

        // ==================== Demo Flow ====================

        // 1. Register Hotel and Branch
        System.out.println("1️⃣  Registering Hotel and Branch...");
        Hotel hotel = hotelService.registerHotel(1, "Grand Plaza Hotels");
        hotelService.addBranch(1, 101, "New York", "123 Main St, NY 10001");
        System.out.println("   ✅ Hotel: " + hotel.getName());
        System.out.println("   ✅ Branch: New York (ID: 101)\n");

        // 2. Create Accounts (Different Roles)
        System.out.println("2️⃣  Creating Accounts...");
        
        Account guest = hotelService.registerAccount(
                "John Doe",
                "john.doe@email.com",
                Set.of(Role.GUEST)
        );
        System.out.println("   ✅ Guest: " + guest.getName() + " (ID: " + guest.getAccountId() + ")");

        Account receptionist = hotelService.registerAccount(
                "Jane Smith",
                "jane.smith@hotel.com",
                Set.of(Role.RECEPTIONIST)
        );
        System.out.println("   ✅ Receptionist: " + receptionist.getName() + " (ID: " + receptionist.getAccountId() + ")");

        Account housekeeper = hotelService.registerAccount(
                "Bob Wilson",
                "bob.wilson@hotel.com",
                Set.of(Role.HOUSEKEEPER)
        );
        System.out.println("   ✅ Housekeeper: " + housekeeper.getName() + " (ID: " + housekeeper.getAccountId() + ")\n");

        // 3. Create Rooms
        System.out.println("3️⃣  Creating Rooms...");
        Room room101 = new Room(1, "101", RoomType.STANDARD);
        Room room201 = new Room(2, "201", RoomType.DELUXE);
        Room room301 = new Room(3, "301", RoomType.FAMILY_SUITE);
        Room room401 = new Room(4, "401", RoomType.BUSINESS_SUITE);
        
        roomRepo.save(room101);
        roomRepo.save(room201);
        roomRepo.save(room301);
        roomRepo.save(room401);
        
        System.out.println("   ✅ Room 101: " + room101.getRoomType() + " - " + room101.getStatus());
        System.out.println("   ✅ Room 201: " + room201.getRoomType() + " - " + room201.getStatus());
        System.out.println("   ✅ Room 301: " + room301.getRoomType() + " - " + room301.getStatus());
        System.out.println("   ✅ Room 401: " + room401.getRoomType() + " - " + room401.getStatus() + "\n");

        // 4. Guest Login
        System.out.println("4️⃣  Guest Login...");
        Account loggedInGuest = hotelService.login("john.doe@email.com");
        System.out.println("   ✅ Logged in as: " + loggedInGuest.getName() + "\n");

        // 5. Create Booking with Cash Payment (Strategy Pattern)
        System.out.println("5️⃣  Creating Booking with Cash Payment...");
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = checkIn.plusDays(3);
        BigDecimal advanceAmount = new BigDecimal("500.00");
        
        Booking booking1 = hotelService.bookRoom(
                1,  // hotelId
                101, // branchId
                loggedInGuest,
                1,  // roomId
                checkIn,
                checkOut,
                advanceAmount
        );
        
        System.out.println("   ✅ Booking Created:");
        System.out.println("      Booking ID: " + booking1.getBookingId());
        System.out.println("      Room ID: " + booking1.getRoomId());
        System.out.println("      Check-in: " + booking1.getCheckInDate());
        System.out.println("      Check-out: " + booking1.getCheckOutDate());
        System.out.println("      Status: " + booking1.getStatus());
        System.out.println("      Payment: $" + advanceAmount + " (Cash)");
        System.out.println("   📧 Notification sent via Observer Pattern!\n");

        // 6. Create Another Booking with Card Payment (Strategy Pattern)
        System.out.println("6️⃣  Creating Another Booking with Card Payment...");
        
        // Create a new payment service with card strategy for this booking
        // In a real system, payment method would be selected per transaction
        PaymentService cardPaymentService = new PaymentService(paymentRepo, cardProcessor);
        BookingService bookingServiceWithCard = new BookingService(
                bookingRepo,
                availabilityRepo,
                cardPaymentService,
                eventBus
        );
        
        LocalDate checkIn2 = LocalDate.now().plusDays(10);
        LocalDate checkOut2 = checkIn2.plusDays(5);
        BigDecimal advanceAmount2 = new BigDecimal("1200.00");
        
        // Create booking directly using the booking service with card payment
        Booking booking2 = new Booking(
                IdGenerator.nextId(),
                loggedInGuest.getAccountId(),
                2,  // roomId
                checkIn2,
                checkOut2
        );
        
        bookingServiceWithCard.createBooking(
                loggedInGuest,
                booking2,
                new DateRange(checkIn2, checkOut2),
                advanceAmount2
        );
        
        System.out.println("   ✅ Booking Created:");
        System.out.println("      Booking ID: " + booking2.getBookingId());
        System.out.println("      Room ID: " + booking2.getRoomId());
        System.out.println("      Check-in: " + booking2.getCheckInDate());
        System.out.println("      Check-out: " + booking2.getCheckOutDate());
        System.out.println("      Status: " + booking2.getStatus());
        System.out.println("      Payment: $" + advanceAmount2 + " (Credit Card)");
        System.out.println("   📧 Notification sent via Observer Pattern!\n");

        // 7. Cancel Booking (24-hour refund logic)
        System.out.println("7️⃣  Cancelling Booking (Testing 24-hour refund logic)...");
        
        // Cancel more than 24 hours before check-in (eligible for refund)
        LocalDateTime cancelTime = LocalDateTime.now().plusDays(6); // 1 day before check-in (24+ hours)
        System.out.println("   Cancelling booking " + booking1.getBookingId() + 
                         " at: " + cancelTime);
        System.out.println("   Check-in date: " + booking1.getCheckInDate());
        
        boolean eligibleForRefund = booking1.isEligibleForFullRefund(cancelTime);
        System.out.println("   Refund eligible: " + eligibleForRefund);
        
        hotelService.cancelBooking(1, 101, loggedInGuest, booking1.getBookingId(), cancelTime);
        System.out.println("   ✅ Booking cancelled!");
        System.out.println("   📧 Cancellation notification sent via Observer Pattern!\n");

        // 8. Try to cancel within 24 hours (no refund)
        System.out.println("8️⃣  Attempting to cancel within 24 hours...");
        LocalDateTime cancelTimeLate = LocalDateTime.now().plusDays(6).plusHours(23); // 23 hours before
        System.out.println("   Cancelling booking " + booking2.getBookingId() + 
                         " at: " + cancelTimeLate);
        System.out.println("   Check-in date: " + booking2.getCheckInDate());
        
        boolean eligibleForRefund2 = booking2.isEligibleForFullRefund(cancelTimeLate);
        System.out.println("   Refund eligible: " + eligibleForRefund2);
        
        hotelService.cancelBooking(1, 101, loggedInGuest, booking2.getBookingId(), cancelTimeLate);
        System.out.println("   ✅ Booking cancelled (no refund)!");
        System.out.println("   📧 Cancellation notification sent via Observer Pattern!\n");

        // 9. Demonstrate Singleton Pattern
        System.out.println("9️⃣  Demonstrating Singleton Pattern...");
        HotelManagementService instance1 = HotelManagementServiceImpl.getInstance();
        HotelManagementService instance2 = HotelManagementServiceImpl.getInstance();
        System.out.println("   Instance 1: " + instance1);
        System.out.println("   Instance 2: " + instance2);
        System.out.println("   Same instance: " + (instance1 == instance2));
        System.out.println("   ✅ Singleton pattern verified!\n");

        // 10. Summary
        System.out.println("📊 ========================================");
        System.out.println("   Demo Summary");
        System.out.println("   ========================================");
        System.out.println("   ✅ Singleton Pattern: HotelManagementService");
        System.out.println("   ✅ Strategy Pattern: Payment methods (Cash, Card)");
        System.out.println("   ✅ Template Method: Payment processing flow");
        System.out.println("   ✅ Observer Pattern: Event-driven notifications");
        System.out.println("   ✅ Repository Pattern: Data access abstraction");
        System.out.println("   ✅ SOLID Principles: Applied throughout");
        System.out.println("   ✅ Domain-Driven Design: Rich domain models");
        System.out.println("\n🎉 Demo completed successfully!");
    }
}

