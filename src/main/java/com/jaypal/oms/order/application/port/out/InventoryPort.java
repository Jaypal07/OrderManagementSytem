package com.jaypal.oms.order.application.port.out;

import java.util.Map;
import java.util.UUID;

/**
 * Port for inventory interaction from Order module.
 */
public interface InventoryPort {

    void reserveStock(UUID orderId, Map<String, Integer> skuQuantities);

    void releaseStock(UUID orderId, Map<String, Integer> skuQuantities);
}
