package com.lld.inventory.facade;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.factory.InventoryItemFactory;
import com.lld.inventory.locking.LockManager;
import com.lld.inventory.observer.LowStockListener;
import com.lld.inventory.observer.LowStockNotifier;
import com.lld.inventory.repository.InMemoryInventoryRepository;
import com.lld.inventory.repository.InventoryRepository;
import com.lld.inventory.service.InventoryService;
import com.lld.inventory.service.InventoryServiceImpl;
import com.lld.inventory.service.WarehouseAvailability;
import com.lld.inventory.strategy.AllEligibleWarehousesStrategy;
import com.lld.inventory.strategy.WarehouseSelectionStrategy;

import java.util.List;

/**
 * Thread-safe Singleton implementation of InventoryFacade.
 * 
 * Wires:
 * - Repository
 * - LockManager
 * - Factory
 * - Strategy
 * - Notifier
 * - Service
 * 
 * Note:
 * - In real systems this wiring is done via DI (Spring)
 * - Singleton is acceptable and expected in LLD interviews
 */
public class InventoryFacadeImpl implements InventoryFacade {
    private static volatile InventoryFacadeImpl instance;
    private static final Object lock = new Object();

    private final InventoryService inventoryService;
    private final LowStockNotifier notifier;

    private InventoryFacadeImpl() {
        // Initialize dependencies
        InventoryRepository repository = new InMemoryInventoryRepository();
        LockManager lockManager = LockManager.getInstance();
        InventoryItemFactory factory = new InventoryItemFactory();
        this.notifier = new LowStockNotifier();
        WarehouseSelectionStrategy strategy = new AllEligibleWarehousesStrategy();

        // Wire service
        this.inventoryService = new InventoryServiceImpl(
                repository,
                lockManager,
                factory,
                notifier,
                strategy
        );
    }

    /**
     * Get singleton instance using double-checked locking pattern.
     * Thread-safe singleton implementation.
     */
    public static InventoryFacadeImpl getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new InventoryFacadeImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void addStock(String warehouseId, String productId, int quantity) {
        inventoryService.addStock(warehouseId, productId, quantity);
    }

    @Override
    public void removeStock(String warehouseId, String productId, int quantity) {
        inventoryService.removeStock(warehouseId, productId, quantity);
    }

    @Override
    public List<WarehouseAvailability> checkAvailability(String productId, int requiredQuantity) {
        return inventoryService.checkAvailability(productId, requiredQuantity);
    }

    @Override
    public void transferStock(String fromWarehouseId, String toWarehouseId, String productId, int quantity) {
        inventoryService.transferStock(fromWarehouseId, toWarehouseId, productId, quantity);
    }

    @Override
    public InventoryItem getInventory(String warehouseId, String productId) {
        return inventoryService.getInventory(warehouseId, productId);
    }

    /**
     * Registers a low-stock listener for receiving alerts.
     * This is exposed for demo purposes. In production, listeners would be registered via DI.
     */
    public void registerLowStockListener(LowStockListener listener) {
        notifier.registerListener(listener);
    }
}

