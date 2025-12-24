
import com.lld.hotel.management.entities.*;
import com.lld.hotel.management.entities.IdGenerator;
import com.lld.hotel.management.observer.BookingCancellationHandler;
import com.lld.hotel.management.observer.EventBus;
import com.lld.hotel.management.observer.NotificationHandler;
import com.lld.hotel.management.pattern.composite.RoomComponent;
import com.lld.hotel.management.pattern.decorator.*;
import com.lld.hotel.management.pattern.factory.NotificationFactory;
import com.lld.hotel.management.pattern.factory.NotificationChannel;
import com.lld.hotel.management.repository.*;
import com.lld.hotel.management.service.*;
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
 * Hotel Management System Demo - Complete Implementation
 * 
 * Demonstrates ALL Design Patterns:
 * - Singleton Pattern (HotelManagementService)
 * - Strategy Pattern (Payment methods: Cash, Card)
 * - Template Method Pattern (Payment processing flow)
 * - Observer Pattern (Event-driven notifications)
 * - Repository Pattern (Data access abstraction)
 * - Builder Pattern (BookingBuilder, InvoiceBuilder) ✅ NEW
 * - Composite Pattern (Room catalog management) ✅ NEW
 * - Decorator Pattern (Price surge modifiers) ✅ NEW
 * - Factory Pattern (Notification channels) ✅ NEW
 * - SOLID Principles
 * - Domain-Driven Design
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("🏨 ========================================");
        System.out.println("   Hotel Management System - Complete Demo");
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
        ServiceRepository serviceRepo = new InMemoryServiceRepository();
        BookingServiceRepository bookingServiceRepo = new InMemoryBookingServiceRepository();
        CleanupTaskRepository cleanupTaskRepo = new InMemoryCleanupTaskRepository();
        RoomKeyRepository roomKeyRepo = new InMemoryRoomKeyRepository();

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
        // Card processor available for future use
        @SuppressWarnings("unused")
        AbstractPaymentProcessor cardProcessor = new StrategyPaymentProcessor(cardStrategy);

        // Services
        PaymentService paymentService = new PaymentService(paymentRepo, cashProcessor);
        BookingService bookingService = new BookingService(
                bookingRepo,
                availabilityRepo,
                paymentService,
                eventBus
        );
        
        // NEW: Additional Services
        AuthenticationService authService = new AuthenticationService(accountRepo);
        AccountService accountService = new AccountService(accountRepo);
        RoomService roomService = new RoomService(roomRepo, availabilityRepo);
        ServiceManagementService serviceManagementService = new ServiceManagementService(
                bookingRepo, serviceRepo, bookingServiceRepo);
        KeyManagementService keyManagementService = new KeyManagementService(roomKeyRepo);
        CleanupTaskService cleanupTaskService = new CleanupTaskService(cleanupTaskRepo, eventBus);

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

        // 2. Create Accounts using AccountService
        System.out.println("2️⃣  Creating Accounts using AccountService...");
        
        Account guest = accountService.createAccount(
                "John Doe",
                "john.doe@email.com",
                Set.of(Role.GUEST)
        );
        System.out.println("   ✅ Guest: " + guest.getName() + " (ID: " + guest.getAccountId() + ")");

        Account receptionist = accountService.createAccount(
                "Jane Smith",
                "jane.smith@hotel.com",
                Set.of(Role.RECEPTIONIST)
        );
        System.out.println("   ✅ Receptionist: " + receptionist.getName() + " (ID: " + receptionist.getAccountId() + ")");

        Account housekeeper = accountService.createAccount(
                "Bob Wilson",
                "bob.wilson@hotel.com",
                Set.of(Role.HOUSEKEEPER)
        );
        System.out.println("   ✅ Housekeeper: " + housekeeper.getName() + " (ID: " + housekeeper.getAccountId() + ")\n");

        // 3. Set passwords for accounts using AuthenticationService
        System.out.println("3️⃣  Setting passwords for accounts using AuthenticationService...");
        authService.setPassword("john.doe@email.com", "password123");
        authService.setPassword("jane.smith@hotel.com", "password123");
        authService.setPassword("bob.wilson@hotel.com", "password123");
        System.out.println("   ✅ All accounts have passwords set\n");

        // 4. Create Rooms
        System.out.println("4️⃣  Creating Rooms...");
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

        // 5. Demonstrate Composite Pattern - Room Catalog
        System.out.println("5️⃣  Demonstrating Composite Pattern - Room Catalog...");
        RoomComponent catalog = roomService.getRoomCatalog();
        System.out.println("   ✅ Room Catalog: " + catalog.getName());
        System.out.println("   ✅ Total Rooms: " + catalog.getRoomCount());
        System.out.println("   ✅ Available Rooms: " + catalog.searchByStatus(RoomStatus.AVAILABLE).size());
        System.out.println("   ✅ Standard Rooms: " + catalog.searchByType(RoomType.STANDARD).size() + "\n");

        // 6. Guest Login using AuthenticationService
        System.out.println("6️⃣  Guest Login using AuthenticationService...");
        AuthenticationService.Session session = authService.login("john.doe@email.com", "password123");
        Account loggedInGuest = authService.validateSession(session.getSessionId())
                .orElseThrow(() -> new IllegalStateException("Session invalid"));
        System.out.println("   ✅ Logged in as: " + loggedInGuest.getName());
        System.out.println("   ✅ Session ID: " + session.getSessionId() + "\n");

        // 7. Demonstrate Builder Pattern - Create Booking
        System.out.println("7️⃣  Demonstrating Builder Pattern - Create Booking...");
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = checkIn.plusDays(3);
        
        Booking booking1 = new BookingBuilder()
                .setBookingId(IdGenerator.nextId())
                .setGuest(loggedInGuest.getAccountId())
                .setRoom(1)
                .setDates(checkIn, checkOut)
                .build();
        
        System.out.println("   ✅ Booking Created using Builder:");
        System.out.println("      Booking ID: " + booking1.getBookingId());
        System.out.println("      Guest ID: " + booking1.getGuestAccountId());
        System.out.println("      Room ID: " + booking1.getRoomId());
        System.out.println("      Dates: " + checkIn + " to " + checkOut + "\n");

        // 8. Process Payment and Confirm Booking
        System.out.println("8️⃣  Processing Payment and Confirming Booking...");
        BigDecimal advanceAmount = new BigDecimal("500.00");
        bookingService.createBooking(
                loggedInGuest,
                booking1,
                new DateRange(checkIn, checkOut),
                advanceAmount
        );
        System.out.println("   ✅ Booking Confirmed!");
        System.out.println("   ✅ Payment: $" + advanceAmount + " (Cash)");
        System.out.println("   📧 Notification sent via Observer Pattern!\n");

        // 9. Demonstrate Decorator Pattern - Price Surge
        System.out.println("9️⃣  Demonstrating Decorator Pattern - Price Surge...");
        BigDecimal basePrice = new BigDecimal("1000.00");
        PriceCalculator base = new BasePrice(basePrice, "Base Room Price");
        
        // Add holiday surge
        PriceCalculator withHoliday = new HolidaySurgeDecorator(base, new BigDecimal("20"));
        System.out.println("   " + withHoliday.getDescription());
        System.out.println("   Price: $" + withHoliday.calculatePrice());
        
        // Add traffic surge on top
        PriceCalculator withHolidayAndTraffic = new TrafficSurgeDecorator(withHoliday, new BigDecimal("15"));
        System.out.println("   " + withHolidayAndTraffic.getDescription());
        System.out.println("   Final Price: $" + withHolidayAndTraffic.calculatePrice() + "\n");

        // 10. Demonstrate Factory Pattern - Notifications
        System.out.println("🔟 Demonstrating Factory Pattern - Notification Channels...");
        NotificationChannel emailChannel = NotificationFactory.createNotification(NotificationFactory.NotificationType.EMAIL);
        NotificationChannel smsChannel = NotificationFactory.createNotification(NotificationFactory.NotificationType.SMS);
        NotificationChannel pushChannel = NotificationFactory.createNotification(NotificationFactory.NotificationType.PUSH);
        
        emailChannel.send("john.doe@email.com", "Your booking is confirmed!");
        smsChannel.send("+1234567890", "Booking confirmed. Check-in: " + checkIn);
        pushChannel.send("device-token-123", "Booking #" + booking1.getBookingId() + " confirmed!");
        System.out.println();

        // 11. Demonstrate ServiceManagementService - Add Services (R8)
        System.out.println("1️⃣1️⃣ Demonstrating ServiceManagementService - Add Services (R8)...");
        
        // Create services
        Service roomServiceEntity = new Service(1, Service.ServiceType.ROOM_SERVICE, 
                "Room Service", "24/7 room service", new BigDecimal("50.00"));
        Service foodService = new Service(2, Service.ServiceType.FOOD_SERVICE, 
                "Food Service", "Restaurant delivery", new BigDecimal("30.00"));
        serviceRepo.save(roomServiceEntity);
        serviceRepo.save(foodService);
        
        // Add services to booking
        BookingServiceEntity bookingService1 = serviceManagementService.addServiceToBooking(
                booking1.getBookingId(), 1, 2); // 2x room service
        BookingServiceEntity bookingService2 = serviceManagementService.addServiceToBooking(
                booking1.getBookingId(), 2, 1); // 1x food service
        
        System.out.println("   ✅ Added Room Service: $" + bookingService1.getTotalPrice());
        System.out.println("   ✅ Added Food Service: $" + bookingService2.getTotalPrice());
        System.out.println("   ✅ Total Services: $" + 
                bookingService1.getTotalPrice().add(bookingService2.getTotalPrice()) + "\n");

        // 12. Demonstrate Builder Pattern - Create Invoice
        System.out.println("1️⃣2️⃣ Demonstrating Builder Pattern - Create Invoice...");
        Invoice invoice = new InvoiceBuilder()
                .setInvoiceId(IdGenerator.nextId())
                .setBooking(booking1.getBookingId())
                .setBaseAmount(advanceAmount)
                .addService("Room Service", new BigDecimal("50.00"), 2)
                .addService("Food Service", new BigDecimal("30.00"), 1)
                .applyTax(new BigDecimal("10")) // 10% tax
                .applyDiscount(new BigDecimal("50.00")) // $50 discount
                .build();
        
        System.out.println("   ✅ Invoice Created using Builder:");
        System.out.println("      Invoice ID: " + invoice.getInvoiceId());
        System.out.println("      Booking ID: " + invoice.getBookingId());
        System.out.println("      Total Amount: $" + invoice.getTotalAmount() + "\n");

        // 13. Demonstrate KeyManagementService (R9)
        System.out.println("1️⃣3️⃣ Demonstrating KeyManagementService - Key Management (R9)...");
        RoomKey roomKey = keyManagementService.generateRoomKey(1);
        System.out.println("   ✅ Room Key Generated:");
        System.out.println("      Key ID: " + roomKey.getKeyId());
        System.out.println("      Key Type: " + roomKey.getKeyType());
        System.out.println("      Can Access Room 1: " + keyManagementService.validateKey(roomKey.getKeyId(), 1));
        
        // Generate master key
        RoomKey masterKey = keyManagementService.generateMasterKey(Set.of(1, 2, 3));
        System.out.println("   ✅ Master Key Generated:");
        System.out.println("      Key ID: " + masterKey.getKeyId());
        System.out.println("      Can Access Room 1: " + keyManagementService.validateKey(masterKey.getKeyId(), 1));
        System.out.println("      Can Access Room 2: " + keyManagementService.validateKey(masterKey.getKeyId(), 2));
        System.out.println("      Can Access Room 5: " + keyManagementService.validateKey(masterKey.getKeyId(), 5) + " (false)\n");

        // 14. Demonstrate CleanupTaskService (R7)
        System.out.println("1️⃣4️⃣ Demonstrating CleanupTaskService - Cleanup Tasks (R7)...");
        CleanupTask task = cleanupTaskService.createCleanupTask(1);
        System.out.println("   ✅ Cleanup Task Created:");
        System.out.println("      Task ID: " + task.getTaskId());
        System.out.println("      Room ID: " + task.getRoomId());
        System.out.println("      Status: " + task.getStatus());
        
        cleanupTaskService.assignTask(task.getTaskId(), housekeeper.getAccountId());
        System.out.println("   ✅ Task Assigned to Housekeeper: " + housekeeper.getName());
        
        cleanupTaskService.completeTask(task.getTaskId());
        System.out.println("   ✅ Task Completed!\n");

        // 15. Demonstrate RoomService - Search Rooms
        System.out.println("1️⃣5️⃣ Demonstrating RoomService - Search Rooms (Composite Pattern)...");
        LocalDate searchCheckIn = LocalDate.now().plusDays(10);
        LocalDate searchCheckOut = searchCheckIn.plusDays(2);
        java.util.List<Room> availableRooms = roomService.searchRooms(
                searchCheckIn, searchCheckOut, RoomType.DELUXE);
        System.out.println("   ✅ Available Deluxe Rooms:");
        availableRooms.forEach(room -> 
                System.out.println("      - Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")"));
        System.out.println();

        // 16. Cancel Booking (24-hour refund logic)
        System.out.println("1️⃣6️⃣ Cancelling Booking (Testing 24-hour refund logic)...");
        LocalDateTime cancelTime = LocalDateTime.now().plusDays(6); // 1 day before check-in (24+ hours)
        boolean eligibleForRefund = booking1.isEligibleForFullRefund(cancelTime);
        System.out.println("   Refund eligible: " + eligibleForRefund);
        
        hotelService.cancelBooking(1, 101, loggedInGuest, booking1.getBookingId(), cancelTime);
        System.out.println("   ✅ Booking cancelled!");
        System.out.println("   📧 Cancellation notification sent via Observer Pattern!\n");

        // 17. Demonstrate Singleton Pattern
        System.out.println("1️⃣7️⃣ Demonstrating Singleton Pattern...");
        HotelManagementService instance1 = HotelManagementServiceImpl.getInstance();
        HotelManagementService instance2 = HotelManagementServiceImpl.getInstance();
        System.out.println("   Instance 1: " + instance1);
        System.out.println("   Instance 2: " + instance2);
        System.out.println("   Same instance: " + (instance1 == instance2));
        System.out.println("   ✅ Singleton pattern verified!\n");

        // 18. Summary
        System.out.println("📊 ========================================");
        System.out.println("   Complete Demo Summary");
        System.out.println("   ========================================");
        System.out.println("   ✅ Singleton Pattern: HotelManagementService");
        System.out.println("   ✅ Strategy Pattern: Payment methods (Cash, Card)");
        System.out.println("   ✅ Template Method: Payment processing flow");
        System.out.println("   ✅ Observer Pattern: Event-driven notifications");
        System.out.println("   ✅ Repository Pattern: Data access abstraction");
        System.out.println("   ✅ Builder Pattern: BookingBuilder, InvoiceBuilder");
        System.out.println("   ✅ Composite Pattern: Room catalog management");
        System.out.println("   ✅ Decorator Pattern: Price surge modifiers");
        System.out.println("   ✅ Factory Pattern: Notification channels");
        System.out.println("   ✅ All Services: Authentication, Account, Room, ServiceManagement, KeyManagement, CleanupTask");
        System.out.println("   ✅ SOLID Principles: Applied throughout");
        System.out.println("   ✅ Domain-Driven Design: Rich domain models");
        System.out.println("   ✅ Concurrency: Thread-safe implementations");
        System.out.println("\n🎉 Complete demo finished successfully!");
        System.out.println("   All 9 design patterns demonstrated!");
        System.out.println("   All 10 requirements implemented!");
    }
}
