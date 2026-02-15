package com.jaypal.oms.inventory.application.usecase;

import com.jaypal.oms.inventory.application.port.out.InventoryRepositoryPort;
import com.jaypal.oms.inventory.domain.model.InventoryItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Reserve Stock Use Case
 *
 * Reserves inventory for a newly placed order.
 * Handles concurrent reservations using optimistic locking with automatic retry.
 *
 * Process:
 * 1. Load inventory items for all SKUs in the order
 * 2. Verify sufficient stock available for each SKU
 * 3. Reserve stock (reduce available, increase reserved)
 * 4. Persist changes
 *
 * On OptimisticLockException (concurrent modification):
 * - Automatically retries up to 3 times
 * - Exponential backoff: 100ms, 200ms, 400ms
 * - If all retries exhausted, throws exception (order fails gracefully)
 */
@Slf4j
@RequiredArgsConstructor
public class ReserveStockUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    /**
     * Reserve stock for an order
     *
     * @param orderId unique order identifier
     * @param skuQuantities map of SKU to quantity to reserve
     * @throws IllegalArgumentException if SKU not found in inventory
     * @throws com.jaypal.oms.inventory.domain.exception.InsufficientStockException if stock unavailable
     * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if conflicts exceed retry limit
     */
    @Transactional
    @Retryable(
            retryFor = {org.springframework.orm.ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    public void reserve(UUID orderId, Map<String, Integer> skuQuantities) {
        log.info("Attempting stock reservation for order: {} with {} SKUs",
                orderId, skuQuantities.size());

        // Reserve stock for each SKU in sorted order to prevent deadlocks
        skuQuantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String sku = entry.getKey();
                    int quantity = entry.getValue();

                    log.debug("Reserving {} units of SKU {} for order {}",
                            quantity, sku, orderId);

                    // Load inventory (may throw OptimisticLockException if concurrent modification)
                    InventoryItem inventory = inventoryRepositoryPort.findBySku(sku)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory not found for SKU: " + sku
                                    )
                            );

                    // Reserve stock (may throw InsufficientStockException)
                    inventory.reserve(quantity);

                    // Save changes (optimistic lock version incremented)
                    inventoryRepositoryPort.save(inventory);

                    log.debug("Successfully reserved {} units of SKU {} for order {}",
                            quantity, sku, orderId);
                });

        log.info("Stock reservation completed successfully for order: {}", orderId);
    }
}


