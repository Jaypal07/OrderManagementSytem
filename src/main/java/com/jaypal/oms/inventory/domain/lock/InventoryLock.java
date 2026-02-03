package com.jaypal.oms.inventory.domain.lock;

/**
 * Represents a lock over inventory state.
 * Lock lifecycle must be explicit.
 */
public interface InventoryLock {

    void lock();

    void unlock();
}
