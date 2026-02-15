package com.jaypal.oms.inventory.infrastructure.persistence;

import com.jaypal.oms.inventory.application.port.out.InventoryRepositoryPort;
import com.jaypal.oms.inventory.domain.model.InventoryItem;
import com.jaypal.oms.inventory.domain.model.StockLevel;

import java.util.Optional;

/**
 * Inventory Repository Adapter
 *
 * Converts between JPA entities and domain models.
 * CRITICAL: toDomain() must reconstruct state without triggering business logic.
 */
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final SpringDataInventoryRepository repository;

    public InventoryRepositoryAdapter(SpringDataInventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<InventoryItem> findBySku(String sku) {
        return repository.findById(sku)
                .map(this::toDomain);
    }

    @Override
    public void save(InventoryItem inventoryItem) {
        InventoryJpaEntity entity = toEntity(inventoryItem);
        repository.save(entity);
    }

    /**
     * Reconstruct InventoryItem from JPA entity without triggering business logic.
     *
     * IMPORTANT: Do NOT call reserve() or release() during reconstruction.
     * These methods modify state and are for business operations only.
     * Instead, create the aggregate root with both available and reserved stock
     * as-is from the database.
     *
     * @param entity the JPA entity
     * @return fully reconstructed InventoryItem
     */
    private InventoryItem toDomain(InventoryJpaEntity entity) {
        // Create item with current available stock
        InventoryItem item = new InventoryItem(
                entity.getSku(),
                new StockLevel(entity.getAvailableStock())
        );

        // Directly set reserved stock to match database state
        // Do NOT call reserve() as it modifies availableStock
        item.setReservedStock(new StockLevel(entity.getReservedStock()));

        return item;
    }

    private InventoryJpaEntity toEntity(InventoryItem item) {
        return new InventoryJpaEntity(
                item.getSku(),
                item.getAvailableStock().getQuantity(),
                item.getReservedStock().getQuantity()
        );
    }
}


