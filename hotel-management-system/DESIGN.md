# Hotel Management System - Design Document

## Domain-Driven Design (DDD) Overview

### Core Domain Entities

#### 1. **Account** (Aggregate Root)
- **Types**: Guest, Receptionist, Housekeeper, Admin
- **Attributes**: accountId, name, email, phone, accountType, createdAt, isActive
- **Responsibilities**: Account data management, profile information, account state
- **Note**: Authentication is handled by AuthenticationService (see Service Layer)

#### 2. **Hotel** (Aggregate Root)
- **Attributes**: hotelId, name, address, branchId, contactInfo
- **Responsibilities**: Manages hotel branches, rooms, and overall operations
- **Pattern**: Singleton (single instance per hotel branch)

#### 3. **Room** (Entity)
- **Attributes**: roomId, roomNumber, roomStyle (Standard, Deluxe, FamilySuite, BusinessSuite), floor, status (Available, Occupied, Maintenance), hotelId
- **Responsibilities**: Room state management, availability tracking
- **Pattern**: Composite (for room catalog management)

#### 4. **Booking** (Aggregate Root)
- **Attributes**: bookingId, guestId, roomId, checkInDate, checkOutDate, duration, status (Confirmed, Cancelled, CheckedIn, CheckedOut), advancePayment, totalAmount, cancellationTime
- **Responsibilities**: Booking lifecycle, cancellation logic, refund calculation
- **Pattern**: Builder (for complex booking construction)

#### 5. **Payment** (Entity)
- **Attributes**: paymentId, bookingId, amount, paymentType, paymentMethod, status, transactionId, timestamp
- **Responsibilities**: Payment processing, refund handling
- **Pattern**: Strategy (payment methods), Template Method (payment flow)

#### 6. **Invoice** (Entity)
- **Attributes**: invoiceId, bookingId, totalAmount, services, taxes, discounts, finalAmount
- **Responsibilities**: Invoice generation, calculation
- **Pattern**: Builder (for complex invoice construction)

#### 7. **Service** (Entity)
- **Types**: RoomService, FoodService, KitchenService, AmenityService
- **Attributes**: serviceId, serviceType, name, description, basePrice
- **Responsibilities**: Service catalog management

#### 8. **BookingService** (Entity)
- **Attributes**: bookingServiceId, bookingId, serviceId, quantity, price, status
- **Responsibilities**: Links bookings with additional services

#### 9. **Key** (Entity)
- **Attributes**: keyId, roomId, keyType (RoomKey, MasterKey), accessLevel, isActive
- **Responsibilities**: Key management, access control

#### 10. **CleanupTask** (Entity)
- **Attributes**: taskId, roomId, housekeeperId, taskType, status, scheduledTime, completedTime
- **Responsibilities**: Task tracking, assignment, completion

#### 11. **Notification** (Value Object)
- **Attributes**: notificationId, recipientId, message, notificationType, status, sentAt
- **Pattern**: Factory (for different notification channels - SMS, Email)

---

## Design Patterns Implementation

### 1. **Observer Pattern - Notifications (R6)**
```
Subject: Booking, Payment, CleanupTask
Observers: NotificationService, EmailNotifier, SMSNotifier
```
- Booking status changes trigger notifications
- Payment confirmations trigger notifications
- Cleanup task assignments notify housekeepers

### 2. **Strategy Pattern - Payment Methods (R4)**
```
PaymentStrategy Interface
├── CashPaymentStrategy
├── CreditCardPaymentStrategy
├── OnlinePaymentStrategy
└── OfflinePaymentStrategy
```
- Each strategy handles payment processing differently
- Easy to add new payment methods

### 3. **Decorator Pattern - Price Surge (R4)**
```
BasePrice
├── HolidaySurgeDecorator
├── TrafficSurgeDecorator
└── SeasonalSurgeDecorator
```
- Dynamically add pricing modifiers
- Composable pricing logic

### 4. **Factory Pattern - Notification Channels (R6)**
```
NotificationFactory
├── createEmailNotification()
├── createSMSNotification()
└── createPushNotification()
```
- Creates appropriate notification type based on requirements
- Extensible for new notification channels

### 5. **Template Method Pattern - Payment Flow**
```
AbstractPaymentProcessor
├── validatePayment()
├── processPayment() [abstract]
├── updateBookingStatus()
└── sendConfirmation()
```
- Common payment flow with specific steps per payment type
- Ensures consistent payment processing

### 6. **Composite Pattern - Room Catalog (R3)**
```
RoomComponent (Interface)
├── Room (Leaf)
├── RoomGroup (Composite)
│   ├── FloorGroup
│   ├── StyleGroup
│   └── AvailabilityGroup
```
- Uniform interface for individual rooms and room groups
- Enables hierarchical room management and search

### 7. **Singleton Pattern - Hotel Instance (R10)**
```
HotelManager (Singleton)
- Ensures single instance per hotel branch
- Global access point for hotel operations
```

### 8. **Builder Pattern - Booking & Invoice Construction**
```
BookingBuilder
├── setGuest()
├── setRoom()
├── setDates()
├── setServices()
└── build()

InvoiceBuilder
├── setBooking()
├── addServices()
├── applyDiscounts()
└── build()
```
- Manages complex object construction
- Ensures valid object creation

### 9. **Dependency Injection**
- Services injected via constructors
- Enables testing and flexibility
- Loose coupling between components

---

## Service Layer Architecture

### Core Services

#### 1. **AuthenticationService** (Account Management)
- `login(email, password)`: Authenticate user and return session/token
- `logout(sessionId)`: Invalidate session
- `register(accountInfo)`: Create new account
- `changePassword(accountId, oldPassword, newPassword)`: Update password
- `validateSession(sessionId)`: Check if session is valid
- `getCurrentUser(sessionId)`: Get authenticated user
- **Note**: Handles all authentication logic, password hashing, session management
- **Why separate?**: Follows SRP - Account entity manages data, AuthenticationService manages security

#### 2. **AccountService** (Account Management)
- `getAccount(accountId)`: Get account details
- `updateProfile(accountId, profileInfo)`: Update account profile
- `deactivateAccount(accountId)`: Deactivate account
- `getAccountsByType(accountType)`: Get accounts by type
- **Note**: Manages account data and profile operations (non-authentication)

#### 3. **BookingService**
- `searchRooms(checkInDate, duration, roomStyle)`: Search available rooms
- `createBooking(bookingRequest)`: Create new booking
- `cancelBooking(bookingId)`: Cancel booking with refund logic
- `checkIn(bookingId)`: Check-in guest
- `checkOut(bookingId)`: Check-out guest

#### 4. **PaymentService**
- `processPayment(paymentRequest)`: Process payment using strategy
- `processRefund(bookingId)`: Handle refunds (R5 - 24hr rule)
- `calculateRefundAmount(bookingId)`: Calculate refund based on cancellation time

#### 5. **NotificationService**
- `notifyBookingConfirmation(bookingId)`: Send booking confirmation
- `notifyCancellation(bookingId)`: Send cancellation notification
- `notifyStatusUpdate(bookingId, status)`: Send status updates

#### 6. **RoomService**
- `getAvailableRooms(criteria)`: Search rooms using composite pattern
- `updateRoomStatus(roomId, status)`: Update room availability
- `assignRoom(bookingId)`: Assign room to booking

#### 7. **ServiceManagementService** (R8)
- `addServiceToBooking(bookingId, serviceId)`: Add service to booking
- `removeServiceFromBooking(bookingId, serviceId)`: Remove service
- `getAvailableServices()`: List available services

#### 8. **KeyManagementService** (R9)
- `generateRoomKey(roomId)`: Generate room-specific key
- `generateMasterKey(roomIds)`: Generate master key for room set
- `validateKey(keyId, roomId)`: Validate key access

#### 9. **CleanupTaskService** (R7)
- `createCleanupTask(roomId, taskType)`: Create cleanup task
- `assignTask(taskId, housekeeperId)`: Assign to housekeeper
- `completeTask(taskId)`: Mark task as complete
- `getTasksByHousekeeper(housekeeperId)`: Get assigned tasks

#### 10. **HotelService** (R10)
- `getHotelBranch(branchId)`: Get hotel branch details
- `getAllBranches()`: List all branches
- `registerBranch(hotelInfo)`: Register new branch

---

## Repository Layer

### Repositories
- `BookingRepository`: CRUD operations for bookings
- `RoomRepository`: Room data access
- `PaymentRepository`: Payment records
- `AccountRepository`: Account data access (no authentication logic)
- `SessionRepository`: Session/token management for authentication
- `ServiceRepository`: Service catalog
- `KeyRepository`: Key management
- `CleanupTaskRepository`: Task management
- `HotelRepository`: Hotel branch data

---

## Domain Events (Observer Pattern Implementation)

### Event Types
1. **BookingConfirmedEvent**: Triggered when booking is confirmed
2. **BookingCancelledEvent**: Triggered when booking is cancelled
3. **PaymentProcessedEvent**: Triggered after payment
4. **CheckInEvent**: Triggered at check-in
5. **CheckOutEvent**: Triggered at check-out
6. **CleanupTaskAssignedEvent**: Triggered when task assigned
7. **CleanupTaskCompletedEvent**: Triggered when task completed

### Event Handlers
- `BookingNotificationHandler`: Handles booking-related notifications
- `PaymentNotificationHandler`: Handles payment confirmations
- `TaskNotificationHandler`: Handles cleanup task notifications

---

## SOLID Principles Application

### Single Responsibility Principle (SRP)
- Each service has a single, well-defined responsibility
- BookingService handles bookings, PaymentService handles payments
- **Account entity** manages account data, **AuthenticationService** manages authentication logic
- Clear separation of concerns

### Open/Closed Principle (OCP)
- Strategy pattern allows adding new payment methods without modifying existing code
- Decorator pattern allows adding new pricing modifiers
- Factory pattern allows adding new notification types

### Liskov Substitution Principle (LSP)
- All payment strategies are interchangeable
- All notification types can be used interchangeably
- Room components (leaf and composite) follow same interface

### Interface Segregation Principle (ISP)
- Focused interfaces for each service
- Clients depend only on methods they use

### Dependency Inversion Principle (DIP)
- Services depend on abstractions (interfaces)
- Dependency injection used throughout
- High-level modules don't depend on low-level modules

---

## Key Design Decisions

### 1. Booking Cancellation Logic (R5)
- Check cancellation time against check-in time
- If > 24 hours: Full refund
- If <= 24 hours: No refund (or partial based on policy)
- Implemented in `PaymentService.calculateRefundAmount()`

### 2. Advance Payment (R4)
- Required during booking creation
- Stored in Booking entity
- Processed via PaymentService using Strategy pattern

### 3. Room Search (R3)
- Uses Composite pattern for flexible room queries
- Supports filtering by style, availability, date range
- Returns available rooms matching criteria

### 4. Multi-branch Support (R10)
- Hotel entity supports branchId
- Singleton pattern ensures one instance per branch
- Branch-specific room and booking management

### 5. Key Management (R9)
- Room-specific keys: One key per room
- Master keys: Can access multiple rooms (defined by accessLevel)
- Key validation before room access

### 6. Service Addition (R8)
- Services can be added to existing bookings
- Each service has base price
- Total invoice recalculated when services added/removed

### 7. Authentication Separation (R1)
- **Account Entity**: Manages account data (name, email, phone, accountType, state)
- **AuthenticationService**: Handles authentication logic (login, password validation, session management)
- **AccountService**: Manages account operations (profile updates, account management)
- **Why separate?**
  - **Single Responsibility Principle**: Account entity focuses on domain data, AuthenticationService focuses on security
  - **Separation of Concerns**: Domain entity vs infrastructure/service concern
  - **Testability**: Easier to mock authentication service for testing
  - **Security**: Centralized authentication logic, easier to secure and audit
  - **DDD Best Practices**: Entities should represent domain concepts, not infrastructure concerns
  - **Flexibility**: Can swap authentication mechanisms (JWT, OAuth, etc.) without changing Account entity

---

## Class Structure Overview

```
com.lld.hotel
├── domain/
│   ├── account/
│   │   ├── Account.java
│   │   └── AccountType.java (enum)
│   ├── hotel/
│   │   └── Hotel.java
│   ├── room/
│   │   ├── Room.java
│   │   ├── RoomStyle.java (enum)
│   │   └── RoomStatus.java (enum)
│   ├── booking/
│   │   ├── Booking.java
│   │   ├── BookingStatus.java (enum)
│   │   └── BookingBuilder.java
│   ├── payment/
│   │   ├── Payment.java
│   │   ├── PaymentType.java (enum)
│   │   └── PaymentStatus.java (enum)
│   ├── invoice/
│   │   ├── Invoice.java
│   │   └── InvoiceBuilder.java
│   ├── service/
│   │   ├── Service.java
│   │   ├── ServiceType.java (enum)
│   │   └── BookingService.java
│   ├── key/
│   │   ├── Key.java
│   │   └── KeyType.java (enum)
│   └── task/
│       ├── CleanupTask.java
│       └── TaskStatus.java (enum)
├── strategy/
│   ├── payment/
│   │   ├── PaymentStrategy.java
│   │   ├── CashPaymentStrategy.java
│   │   ├── CreditCardPaymentStrategy.java
│   │   ├── OnlinePaymentStrategy.java
│   │   └── OfflinePaymentStrategy.java
│   └── pricing/
│       ├── PriceCalculator.java
│       ├── BasePrice.java
│       ├── HolidaySurgeDecorator.java
│       ├── TrafficSurgeDecorator.java
│       └── SeasonalSurgeDecorator.java
├── pattern/
│   ├── composite/
│   │   ├── RoomComponent.java
│   │   ├── Room.java (implements RoomComponent)
│   │   └── RoomGroup.java (implements RoomComponent)
│   ├── observer/
│   │   ├── EventBus.java
│   │   ├── Event.java
│   │   ├── EventHandler.java
│   │   └── events/
│   │       ├── BookingConfirmedEvent.java
│   │       ├── BookingCancelledEvent.java
│   │       └── ...
│   ├── factory/
│   │   └── NotificationFactory.java
│   └── template/
│       ├── PaymentProcessor.java
│       └── AbstractPaymentProcessor.java
├── service/
│   ├── AuthenticationService.java
│   ├── AccountService.java
│   ├── BookingService.java
│   ├── PaymentService.java
│   ├── NotificationService.java
│   ├── RoomService.java
│   ├── ServiceManagementService.java
│   ├── KeyManagementService.java
│   ├── CleanupTaskService.java
│   └── HotelService.java
├── repository/
│   ├── BookingRepository.java
│   ├── RoomRepository.java
│   ├── PaymentRepository.java
│   ├── AccountRepository.java
│   ├── ServiceRepository.java
│   ├── KeyRepository.java
│   ├── CleanupTaskRepository.java
│   └── HotelRepository.java
├── notification/
│   ├── NotificationChannel.java
│   ├── EmailNotification.java
│   ├── SMSNotification.java
│   └── PushNotification.java
└── facade/
    └── HotelManagementSystem.java (Singleton)
```

---

## Sequence Diagrams (Key Flows)

### Booking Flow
1. Guest searches for rooms (RoomService)
2. Guest creates booking (BookingService)
3. Advance payment processed (PaymentService with Strategy)
4. Booking confirmed (BookingService)
5. Notification sent (NotificationService with Factory)
6. Booking status updated

### Cancellation Flow
1. Guest requests cancellation (BookingService)
2. Calculate refund based on 24hr rule (PaymentService)
3. Process refund using payment strategy (PaymentService)
4. Update booking status (BookingService)
5. Send cancellation notification (NotificationService)
6. Update room availability (RoomService)

### Cleanup Task Flow
1. Check-out triggers cleanup task creation (CleanupTaskService)
2. Task assigned to housekeeper (CleanupTaskService)
3. Housekeeper notified (NotificationService)
4. Task completed (CleanupTaskService)
5. Room status updated to Available (RoomService)

---

## Testing Considerations

- All services use dependency injection for testability
- Repository interfaces allow mocking
- Strategy pattern enables testing individual payment methods
- Observer pattern allows testing event handling separately
- Builder pattern ensures valid object creation in tests

---

## Extensibility Points

1. **New Payment Methods**: Implement PaymentStrategy interface
2. **New Notification Channels**: Extend NotificationFactory
3. **New Pricing Modifiers**: Create new Decorator classes
4. **New Room Styles**: Add to RoomStyle enum
5. **New Service Types**: Add to ServiceType enum
6. **New Account Types**: Add to AccountType enum

---

This design provides a solid foundation that follows DDD principles, incorporates all requested design patterns, and adheres to SOLID principles while meeting all functional requirements.

