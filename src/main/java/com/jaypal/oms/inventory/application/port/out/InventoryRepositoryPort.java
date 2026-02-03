package com.jaypal.oms.inventory.application.port.out;

import com.jaypal.oms.inventory.domain.model.InventoryItem;

import java.util.Optional;

/**
 * Port for inventory persistence.
 */
public interface InventoryRepositoryPort {

    Optional<InventoryItem> findBySku(String sku);

    void save(InventoryItem inventoryItem);
}
