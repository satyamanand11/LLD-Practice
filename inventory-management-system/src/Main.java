import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.facade.InventoryFacade;
import com.lld.inventory.facade.InventoryFacadeImpl;
import com.lld.inventory.observer.LowStockListener;
import com.lld.inventory.observer.impl.ConsoleLogListener;
import com.lld.inventory.observer.impl.EmailAlertListener;
import com.lld.inventory.service.WarehouseAvailability;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main class demonstrating Inventory Management System
 * 
 * This demonstrates:
 * - Thread-safe inventory operations
 * - Multiple warehouses and products
 * - Add/Remove stock operations
 * - Stock transfers between warehouses
 * - Availability checking
 * - Low-stock alerts (Observer pattern)
 * - Concurrent operations
 * - Design Patterns: Facade, Repository, Strategy, Observer, Factory, Singleton
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Inventory Management System ===\n");

        // Get singleton facade instance (Facade Pattern)
        InventoryFacade facade = InventoryFacadeImpl.getInstance();
        System.out.println("✓ Using Facade Pattern: InventoryFacade\n");

        // ========== SETUP: Register Alert Listeners ==========
        System.out.println("1. Registering Low-Stock Alert Listeners (Observer Pattern)");
        InventoryFacadeImpl facadeImpl = (InventoryFacadeImpl) facade;
        LowStockListener consoleListener = new ConsoleLogListener();
        LowStockListener emailListener = new EmailAlertListener();
        facadeImpl.registerLowStockListener(consoleListener);
        facadeImpl.registerLowStockListener(emailListener);
        System.out.println("   Registered: ConsoleLogListener, EmailAlertListener\n");

        // ========== DEMONSTRATION ==========

        System.out.println("2. Adding Stock to Warehouses (Receiving Shipments)");
        System.out.println("   Adding 100 units of Product P1 to Warehouse W1");
        facade.addStock("W1", "P1", 100);
        printInventory(facade, "W1", "P1");

        System.out.println("   Adding 50 units of Product P1 to Warehouse W2");
        facade.addStock("W2", "P1", 50);
        printInventory(facade, "W2", "P1");

        System.out.println("   Adding 200 units of Product P2 to Warehouse W1");
        facade.addStock("W1", "P2", 200);
        printInventory(facade, "W1", "P2");

        System.out.println("   Adding 75 units of Product P2 to Warehouse W3");
        facade.addStock("W3", "P2", 75);
        printInventory(facade, "W3", "P2");
        System.out.println();

        System.out.println("3. Checking Availability");
        System.out.println("   Checking availability for Product P1, Quantity: 60");
        List<WarehouseAvailability> availability = facade.checkAvailability("P1", 60);
        System.out.println("   Warehouses that can fulfill:");
        for (WarehouseAvailability wa : availability) {
            System.out.println("     - Warehouse " + wa.getWarehouseId() + 
                             ": " + wa.getAvailableQuantity() + " units available");
        }
        System.out.println();

        System.out.println("4. Removing Stock (Fulfilling Orders)");
        System.out.println("   Removing 30 units of Product P1 from Warehouse W1");
        facade.removeStock("W1", "P1", 30);
        printInventory(facade, "W1", "P1");
        System.out.println();

        System.out.println("5. Transferring Stock Between Warehouses");
        System.out.println("   Transferring 20 units of Product P1 from W1 to W2");
        facade.transferStock("W1", "W2", "P1", 20);
        System.out.println("   After transfer:");
        printInventory(facade, "W1", "P1");
        printInventory(facade, "W2", "P1");
        System.out.println();

        System.out.println("6. Testing Low-Stock Alerts");
        System.out.println("   Reducing stock to trigger low-stock alert (threshold: 10)");
        System.out.println("   Current stock in W1 for P1: " + 
                         (facade.getInventory("W1", "P1") != null ? 
                          facade.getInventory("W1", "P1").getQuantity() : 0));
        System.out.println("   Removing stock to bring it below threshold...");
        
        // Remove stock to trigger low-stock alert
        InventoryItem item = facade.getInventory("W1", "P1");
        if (item != null && item.getQuantity() > 10) {
            int toRemove = item.getQuantity() - 5; // Bring it to 5 (below threshold of 10)
            facade.removeStock("W1", "P1", toRemove);
            printInventory(facade, "W1", "P1");
            System.out.println("   ✓ Low-stock alert should have been triggered above\n");
        }

        System.out.println("7. Testing Negative Inventory Prevention");
        System.out.println("   Attempting to remove more stock than available...");
        try {
            facade.removeStock("W2", "P1", 1000); // Try to remove 1000 when we have less
            System.out.println("   ERROR: Should have thrown exception!");
        } catch (IllegalStateException e) {
            System.out.println("   ✓ Correctly rejected: " + e.getMessage() + "\n");
        }

        System.out.println("8. Testing Concurrent Operations (Thread Safety)");
        System.out.println("   Running 10 concurrent threads adding/removing stock...");
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // Get initial quantity
        InventoryItem initialItem = facade.getInventory("W1", "P2");
        int initialQty = initialItem != null ? initialItem.getQuantity() : 0;
        System.out.println("   Initial quantity: " + initialQty);
        
        // Submit concurrent operations
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    facade.addStock("W1", "P2", 10);
                } catch (Exception e) {
                    System.err.println("Error in add: " + e.getMessage());
                }
            });
        }
        
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    facade.removeStock("W1", "P2", 5);
                } catch (Exception e) {
                    System.err.println("Error in remove: " + e.getMessage());
                }
            });
        }
        
        // Wait for all tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check final quantity
        InventoryItem finalItem = facade.getInventory("W1", "P2");
        int finalQty = finalItem != null ? finalItem.getQuantity() : 0;
        int expectedQty = initialQty + (5 * 10) - (5 * 5); // 5 adds of 10, 5 removes of 5
        
        System.out.println("   Final quantity: " + finalQty);
        System.out.println("   Expected quantity: " + expectedQty);
        if (finalQty == expectedQty) {
            System.out.println("   ✓ Thread-safety verified: Final quantity matches expected\n");
        } else {
            System.out.println("   ✗ Thread-safety issue: Final quantity does not match expected\n");
        }

        System.out.println("9. Final Inventory State");
        System.out.println("   Warehouse W1:");
        printInventory(facade, "W1", "P1");
        printInventory(facade, "W1", "P2");
        System.out.println("   Warehouse W2:");
        printInventory(facade, "W2", "P1");
        System.out.println("   Warehouse W3:");
        printInventory(facade, "W3", "P2");
        System.out.println();

        System.out.println("=== Demo Complete ===");
        System.out.println("\nKey Features Demonstrated:");
        System.out.println("✓ Add stock to warehouses");
        System.out.println("✓ Remove stock from warehouses");
        System.out.println("✓ Check availability across warehouses");
        System.out.println("✓ Transfer stock between warehouses");
        System.out.println("✓ Low-stock alerts (Observer pattern)");
        System.out.println("✓ Negative inventory prevention");
        System.out.println("✓ Thread-safe concurrent operations");
        System.out.println("\nDesign Patterns Used:");
        System.out.println("✓ Facade Pattern: Unified interface through InventoryFacade");
        System.out.println("✓ Repository Pattern: Data access abstraction");
        System.out.println("✓ Strategy Pattern: WarehouseSelectionStrategy for availability");
        System.out.println("✓ Observer Pattern: LowStockNotifier/LowStockListener for alerts");
        System.out.println("✓ Factory Pattern: InventoryItemFactory for creation");
        System.out.println("✓ Singleton Pattern: Thread-safe facade instance");
        System.out.println("\nArchitecture Highlights:");
        System.out.println("✓ InventoryKey as value object (immutable, Comparable)");
        System.out.println("✓ Fine-grained locking per inventory item");
        System.out.println("✓ Sorted lock ordering for deadlock prevention");
        System.out.println("✓ Clear separation: Repository (data) vs Service (business logic)");
        System.out.println("✓ Thread-safe operations with LockManager");
    }

    private static void printInventory(InventoryFacade facade, String warehouseId, String productId) {
        InventoryItem item = facade.getInventory(warehouseId, productId);
        if (item != null) {
            System.out.println("     Warehouse: " + warehouseId + 
                             ", Product: " + productId + 
                             ", Quantity: " + item.getQuantity() + 
                             ", Threshold: " + item.getLowStockThreshold() +
                             ", Low Stock: " + item.isLow());
        } else {
            System.out.println("     Warehouse: " + warehouseId + 
                             ", Product: " + productId + 
                             " - No inventory found");
        }
    }
}

