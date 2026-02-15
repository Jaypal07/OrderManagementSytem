package com.jaypal.oms.inventory.application;

import com.jaypal.oms.inventory.api.InventoryApi;
import com.jaypal.oms.inventory.infrastructure.persistence.InventoryJpaEntity;
import com.jaypal.oms.inventory.infrastructure.persistence.SpringDataInventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryApi {

    private final SpringDataInventoryRepository repository;

    public InventoryServiceImpl(SpringDataInventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void reserveStock(UUID orderId, Map<String, Integer> skuQuantities) {
        log.info("Reserving stock for order: {}", orderId);

        // Deadlock prevention: Sort Lock Order
        skuQuantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String sku = entry.getKey();
                    int requestedQty = entry.getValue();

                    InventoryJpaEntity item = repository.findBySku(sku)
                            .orElseThrow(() -> new RuntimeException("SKU not found: " + sku));

                    if (item.getAvailableStock() < requestedQty) {
                        throw new RuntimeException("Insufficient stock for SKU: " + sku);
                    }

                    item.setAvailableStock(item.getAvailableStock() - requestedQty);
                    item.setReservedStock(item.getReservedStock() + requestedQty);
                    repository.save(item);
                });

        log.info("Stock reserved successfully for order: {}", orderId);
    }

    @Override
    @Transactional
    public void releaseStock(UUID orderId) {
        log.info("Releasing stock for order: {}", orderId);
        log.warn("Release stock not implemented without Reservation table tracking.");
    }
}
