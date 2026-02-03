package com.jaypal.oms.order.application.port.in;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Command object for placing an order.
 * Represents the input to the PlaceOrder use case.
 */
public final class PlaceOrderCommand {

    private final Map<String, Integer> skuQuantities;

    public PlaceOrderCommand(Map<String, Integer> skuQuantities) {
        if (skuQuantities == null || skuQuantities.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        skuQuantities.forEach((sku, qty) -> {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("SKU must be provided");
            }
            if (qty == null || qty <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
        });

        this.skuQuantities = Map.copyOf(skuQuantities);
    }

    public Map<String, Integer> getSkuQuantities() {
        return Collections.unmodifiableMap(skuQuantities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceOrderCommand)) return false;
        PlaceOrderCommand that = (PlaceOrderCommand) o;
        return skuQuantities.equals(that.skuQuantities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skuQuantities);
    }
}
