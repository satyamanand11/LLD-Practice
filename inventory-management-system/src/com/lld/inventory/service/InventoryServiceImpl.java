package com.lld.inventory.service;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.domain.InventoryKey;
import com.lld.inventory.factory.InventoryItemFactory;
import com.lld.inventory.locking.LockManager;
import com.lld.inventory.observer.LowStockNotifier;
import com.lld.inventory.repository.InventoryRepository;
import com.lld.inventory.strategy.WarehouseSelectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of InventoryService.
 * 
 * Responsibilities:
 * - Acquire/release locks
 * - Create inventory rows if missing
 * - Reject invalid operations
 * - Notify low-stock observers
 */
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository repository;
    private final LockManager lockManager;
    private final InventoryItemFactory factory;
    private final LowStockNotifier notifier;
    private final WarehouseSelectionStrategy selectionStrategy;

    public InventoryServiceImpl(
            InventoryRepository repository,
            LockManager lockManager,
            InventoryItemFactory factory,
            LowStockNotifier notifier,
            WarehouseSelectionStrategy selectionStrategy) {
        if (repository == null) {
            throw new IllegalArgumentException("repository cannot be null");
        }
        if (lockManager == null) {
            throw new IllegalArgumentException("lockManager cannot be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("notifier cannot be null");
        }
        if (selectionStrategy == null) {
            throw new IllegalArgumentException("selectionStrategy cannot be null");
        }
        this.repository = repository;
        this.lockManager = lockManager;
        this.factory = factory;
        this.notifier = notifier;
        this.selectionStrategy = selectionStrategy;
    }

    @Override
    public void addStock(String warehouseId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        InventoryKey key = new InventoryKey(warehouseId, productId);
        lockManager.executeWithLock(key, () -> {
            Optional<InventoryItem> existing = repository.findByKey(key);
            InventoryItem item;
            if (existing.isPresent()) {
                item = existing.get();
                item.increase(quantity);
            } else {
                // Create new inventory row if missing
                item = factory.create(key, quantity);
            }
            repository.upsert(item);

            // Check and notify if low stock
            if (item.isLow()) {
                notifier.notifyLowStock(item);
            }
        });
    }

    @Override
    public void removeStock(String warehouseId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        InventoryKey key = new InventoryKey(warehouseId, productId);
        lockManager.executeWithLock(key, () -> {
            Optional<InventoryItem> existing = repository.findByKey(key);
            if (existing.isEmpty()) {
                throw new IllegalStateException(
                        String.format("Inventory not found for warehouse: %s, product: %s",
                                warehouseId, productId));
            }

            InventoryItem item = existing.get();
            
            // Validate sufficient stock before removing
            if (item.getQuantity() < quantity) {
                throw new IllegalStateException(
                        String.format("Insufficient stock. Requested: %d, Available: %d. Key: %s",
                                quantity, item.getQuantity(), key));
            }

            item.decrease(quantity);
            repository.upsert(item);

            // Check and notify if low stock
            if (item.isLow()) {
                notifier.notifyLowStock(item);
            }
        });
    }

    @Override
    public void transferStock(String fromWarehouseId, String toWarehouseId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new IllegalArgumentException("Source and destination warehouses cannot be the same");
        }

        InventoryKey fromKey = new InventoryKey(fromWarehouseId, productId);
        InventoryKey toKey = new InventoryKey(toWarehouseId, productId);

        // Lock both keys in sorted order to prevent deadlocks
        List<InventoryKey> keys = new ArrayList<>();
        keys.add(fromKey);
        keys.add(toKey);

        lockManager.executeWithLocks(keys, () -> {
            // Get source inventory
            Optional<InventoryItem> fromItemOpt = repository.findByKey(fromKey);
            if (fromItemOpt.isEmpty()) {
                throw new IllegalStateException(
                        String.format("Source inventory not found for warehouse: %s, product: %s",
                                fromWarehouseId, productId));
            }

            InventoryItem fromItem = fromItemOpt.get();

            // Validate sufficient stock in source
            if (fromItem.getQuantity() < quantity) {
                throw new IllegalStateException(
                        String.format("Insufficient stock in source warehouse. Requested: %d, Available: %d. Key: %s",
                                quantity, fromItem.getQuantity(), fromKey));
            }

            // Remove from source
            fromItem.decrease(quantity);
            repository.upsert(fromItem);

            // Add to destination (create if missing)
            Optional<InventoryItem> toItemOpt = repository.findByKey(toKey);
            InventoryItem toItem;
            if (toItemOpt.isPresent()) {
                toItem = toItemOpt.get();
                toItem.increase(quantity);
            } else {
                // Create new inventory row if missing
                toItem = factory.create(toKey, quantity);
            }
            repository.upsert(toItem);

            // Check and notify if low stock (for both source and destination)
            if (fromItem.isLow()) {
                notifier.notifyLowStock(fromItem);
            }
            if (toItem.isLow()) {
                notifier.notifyLowStock(toItem);
            }
        });
    }

    @Override
    public List<WarehouseAvailability> checkAvailability(String productId, int requiredQuantity) {
        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("requiredQuantity must be positive");
        }

        // Read-only operation - no locking needed for reads
        // In a real system with read-write locks, we'd use read lock here
        List<InventoryItem> allItems = repository.findByProductId(productId);

        // Filter items with sufficient stock
        List<InventoryItem> eligibleItems = new ArrayList<>();
        for (InventoryItem item : allItems) {
            if (item.getQuantity() >= requiredQuantity) {
                eligibleItems.add(item);
            }
        }

        // Use strategy to select warehouses
        List<String> selectedWarehouseIds = selectionStrategy.selectWarehouses(eligibleItems, requiredQuantity);

        // Build result list
        List<WarehouseAvailability> result = new ArrayList<>();
        for (InventoryItem item : eligibleItems) {
            if (selectedWarehouseIds.contains(item.getKey().getWarehouseId())) {
                result.add(new WarehouseAvailability(
                        item.getKey().getWarehouseId(),
                        item.getQuantity()
                ));
            }
        }

        return result;
    }

    @Override
    public InventoryItem getInventory(String warehouseId, String productId) {
        InventoryKey key = new InventoryKey(warehouseId, productId);
        // Read operation - no locking needed for single read
        // In a real system with read-write locks, we'd use read lock here
        return repository.findByKey(key).orElse(null);
    }
}

