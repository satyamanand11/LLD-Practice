# Parking Lot Management System - Low Level Design

## Problem Statement

Design a parking lot management system that can handle multiple types of vehicles and parking spots with different strategies and features. The system should be thread-safe, scalable, and follow SOLID principles with proper design patterns.

### Requirements

1. **Vehicle Types**: Car, Truck, Motorbike
2. **Parking Spot Types**: Mini, Compact, Large
3. **Parking Strategies**: Nearest First, Farthest First
4. **Additional Features**: Electric charging, Car wash
5. **Concurrency**: Handle multiple vehicles parking simultaneously
6. **Payment**: Support for Cash and Credit Card payments
7. **Display**: Real-time display of available spots
8. **Accounts**: Admin and Parking Attendant accounts

### Key Features

- **Thread-Safe Operations**: Multiple vehicles can park/unpark concurrently
- **Strategy Pattern**: Pluggable parking strategies
- **Decorator Pattern**: Add features like electric charging and car wash
- **Observer Pattern**: Real-time updates to display board
- **State Pattern**: Manage parking spot states
- **Repository Pattern**: Abstract data access layer
- **Command Pattern**: Encapsulate parking operations
- **Builder Pattern**: Create complex objects

## System Architecture

### Core Components

1. **ParkingLot**: Central singleton managing the entire system
2. **ParkingService**: Business logic for parking operations
3. **PaymentService**: Handle payment processing
4. **DisplayService**: Manage display board updates
5. **ParkingSpotRepository**: Data access layer for parking spots

### Design Patterns Used

- **Singleton**: ParkingLot, DisplayBoard
- **Strategy**: Parking strategies (NearestFirst, FarthestFirst)
- **Decorator**: SpotDecorator for additional features
- **Observer**: Real-time display updates
- **State**: ParkingSpotState management
- **Repository**: Data access abstraction
- **Command**: Parking operations encapsulation
- **Builder**: Complex object creation