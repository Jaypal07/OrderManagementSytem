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
 * Release Stock Use Case
 *
 * Releases previously reserved inventory when an order is cancelled.
 * Handles concurrent modifications using optimistic locking with automatic retry.
 *
 * Process:
 * 1. Load inventory items for all SKUs in the order
 * 2. Release reserved stock (increase available, decrease reserved)
 * 3. Persist changes
 *
 * On OptimisticLockException (concurrent modification):
 * - Automatically retries up to 3 times
 * - Exponential backoff: 100ms, 200ms, 400ms
 * - If all retries exhausted, throws exception (cancellation fails, manual intervention needed)
 */
@Slf4j
@RequiredArgsConstructor
public class ReleaseStockUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    /**
     * Release reserved stock for a cancelled order
     *
     * @param orderId unique order identifier
     * @param skuQuantities map of SKU to quantity to release
     * @throws IllegalArgumentException if SKU not found in inventory
     * @throws IllegalStateException if reserved stock is less than release amount
     * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if conflicts exceed retry limit
     */
    @Transactional
    @Retryable(
            retryFor = {org.springframework.orm.ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    public void release(UUID orderId, Map<String, Integer> skuQuantities) {
        log.info("Attempting stock release for cancelled order: {} with {} SKUs",
                orderId, skuQuantities.size());

        // Release stock for each SKU in sorted order to prevent deadlocks
        skuQuantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String sku = entry.getKey();
                    int quantity = entry.getValue();

                    log.debug("Releasing {} units of SKU {} from order {}",
                            quantity, sku, orderId);

                    // Load inventory (may throw OptimisticLockException if concurrent modification)
                    InventoryItem inventory = inventoryRepositoryPort.findBySku(sku)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory not found for SKU: " + sku
                                    )
                            );

                    // Release stock (may throw IllegalStateException if reserved < quantity)
                    inventory.release(quantity);

                    // Save changes (optimistic lock version incremented)
                    inventoryRepositoryPort.save(inventory);

                    log.debug("Successfully released {} units of SKU {} from order {}",
                            quantity, sku, orderId);
                });

        log.info("Stock release completed successfully for cancelled order: {}", orderId);
    }
}


