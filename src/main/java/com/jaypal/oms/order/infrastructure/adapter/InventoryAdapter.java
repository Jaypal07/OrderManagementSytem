package com.jaypal.oms.order.infrastructure.adapter;

import com.jaypal.oms.inventory.api.InventoryApi;
import com.jaypal.oms.order.application.port.out.InventoryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class InventoryAdapter implements InventoryPort {

    private final InventoryApi inventoryApi;

    public InventoryAdapter(InventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    @Override
    public void reserveStock(UUID orderId, Map<String, Integer> skuQuantities) {
        inventoryApi.reserveStock(orderId, skuQuantities);
    }

    @Override
    public void releaseStock(UUID orderId) {
        inventoryApi.releaseStock(orderId);
    }
}
