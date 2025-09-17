# LLD Practice Repository

This repository contains Low Level Design (LLD) implementations and practice projects.

## Projects

### 1. Parking Lot Management System
**Location**: `parking-lot-management-system/`

A comprehensive parking lot management system implementing SOLID principles, design patterns, and thread safety.

**Features**:
- Thread-safe operations for concurrent vehicle parking
- Multiple design patterns (Singleton, Strategy, Decorator, Observer, State, Repository, Command, Builder)
- SOLID principles compliance
- Support for different vehicle types (Car, Truck, Motorbike)
- Multiple parking strategies (Nearest First, Farthest First)
- Additional features (Electric charging, Car wash)
- Real-time display board updates
- Payment processing (Cash and Credit Card)

**Key Design Patterns**:
- **Singleton**: ParkingLot, DisplayBoard
- **Strategy**: Parking strategies
- **Decorator**: SpotDecorator for additional features
- **Observer**: Real-time display updates
- **State**: ParkingSpotState management
- **Repository**: Data access abstraction
- **Command**: Parking operations encapsulation
- **Builder**: Complex object creation

**Architecture**:
- Thread-safe implementation with proper synchronization
- Dependency injection for loose coupling
- Comprehensive error handling and validation
- Clean separation of concerns

For detailed documentation, see [parking-lot-management-system/README.md](parking-lot-management-system/README.md)

## Getting Started

Each project in this repository is self-contained and can be run independently. Navigate to the specific project directory for detailed setup instructions.

## Contributing

Feel free to add more LLD practice projects to this repository. Each project should be in its own directory with proper documentation.