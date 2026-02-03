package com.jaypal.oms.inventory.application.port.in;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Command for reserving stock for an order.
 */
public final class ReserveStockCommand {

    private final UUID orderId;
    private final Map<String, Integer> skuQuantities;

    public ReserveStockCommand(UUID orderId, Map<String, Integer> skuQuantities) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId must be provided");
        }
        if (skuQuantities == null || skuQuantities.isEmpty()) {
            throw new IllegalArgumentException("Stock reservation must contain items");
        }

        skuQuantities.forEach((sku, qty) -> {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("SKU must be provided");
            }
            if (qty == null || qty <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
        });

        this.orderId = orderId;
        this.skuQuantities = Map.copyOf(skuQuantities);
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Map<String, Integer> getSkuQuantities() {
        return Collections.unmodifiableMap(skuQuantities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReserveStockCommand)) return false;
        ReserveStockCommand that = (ReserveStockCommand) o;
        return orderId.equals(that.orderId) &&
                skuQuantities.equals(that.skuQuantities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, skuQuantities);
    }
}
