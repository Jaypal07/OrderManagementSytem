package com.jaypal.oms.inventory.application.port.in;

import com.jaypal.oms.inventory.application.port.out.InventoryRepositoryPort;
import com.jaypal.oms.inventory.domain.model.InventoryItem;

import java.util.Map;

/**
 * Use case for releasing previously reserved stock.
 */
public class ReleaseStockUseCase {

    private final InventoryRepositoryPort inventoryRepository;

    public ReleaseStockUseCase(InventoryRepositoryPort inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Releases reserved stock.
     * This is typically triggered when an order is cancelled.
     */
    public void release(Map<String, Integer> skuQuantities) {

        for (Map.Entry<String, Integer> entry : skuQuantities.entrySet()) {

            String sku = entry.getKey();
            int quantity = entry.getValue();

            InventoryItem inventoryItem = inventoryRepository.findBySku(sku)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Inventory not found for SKU: " + sku)
                    );

            inventoryItem.release(quantity);

            inventoryRepository.save(inventoryItem);
        }
    }
}
