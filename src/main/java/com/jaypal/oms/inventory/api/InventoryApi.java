package com.jaypal.oms.inventory.api;

import java.util.Map;
import java.util.UUID;

public interface InventoryApi {
    /**
     * Reserves stock for a given order.
     * 
     * @throws RuntimeException if stock is insufficient.
     */
    void reserveStock(UUID orderId, Map<String, Integer> skuQuantities);

    /**
     * Releases reserved stock for a given order (e.g. on cancellation).
     */
    void releaseStock(UUID orderId);
}
