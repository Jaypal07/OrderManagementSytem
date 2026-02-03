package com.jaypal.oms.inventory.infrastructure.persistence;

import com.jaypal.oms.inventory.application.port.out.InventoryRepositoryPort;
import com.jaypal.oms.inventory.domain.model.InventoryItem;
import com.jaypal.oms.inventory.domain.model.StockLevel;

import java.util.Optional;

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

    private InventoryItem toDomain(InventoryJpaEntity entity) {
        InventoryItem item = new InventoryItem(
                entity.getSku(),
                new StockLevel(entity.getAvailableStock())
        );

        if (entity.getReservedStock() > 0) {
            item.reserve(entity.getReservedStock());
        }

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
