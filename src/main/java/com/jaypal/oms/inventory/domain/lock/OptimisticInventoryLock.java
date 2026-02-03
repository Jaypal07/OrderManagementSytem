package com.jaypal.oms.inventory.domain.lock;

/**
 * Marker implementation for optimistic locking.
 * Actual locking is handled by persistence layer.
 */
public class OptimisticInventoryLock implements InventoryLock {

    @Override
    public void lock() {
        // No-op for optimistic locking
    }

    @Override
    public void unlock() {
        // No-op for optimistic locking
    }
}
