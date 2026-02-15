package com.jaypal.oms.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Inventory persistence
 *
 * Uses optimistic locking via @Version on InventoryJpaEntity.
 * Optimistic locking allows higher concurrency with automatic retry.
 * On conflict (OptimisticLockException), Spring Retry will handle retries.
 */
public interface SpringDataInventoryRepository extends JpaRepository<InventoryJpaEntity, String> {

    /**
     * Find inventory by SKU
     *
     * No explicit lock required - @Version on entity handles optimistic locking.
     * If version conflicts occur, OptimisticLockException is thrown and
     * caught by @Retryable in the use case layer.
     *
     * @param sku the product SKU
     * @return Optional containing the inventory item if found
     */
    Optional<InventoryJpaEntity> findBySku(String sku);

    @Override
    Optional<InventoryJpaEntity> findById(String sku);
}
