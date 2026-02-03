package com.jaypal.oms.order.domain.model;

import com.jaypal.oms.shared.kernel.DomainException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a single item in an order.
 * Immutable by design.
 */
public final class OrderItem {

    private final String sku;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String sku, int quantity, BigDecimal unitPrice) {
        if (sku == null || sku.isBlank()) {
            throw new InvalidOrderItemException("SKU must be provided");
        }
        if (quantity <= 0) {
            throw new InvalidOrderItemException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new InvalidOrderItemException("Unit price must be greater than zero");
        }

        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal totalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private static class InvalidOrderItemException extends DomainException {
        private InvalidOrderItemException(String message) {
            super(message);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return quantity == that.quantity &&
                sku.equals(that.sku) &&
                unitPrice.equals(that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, quantity, unitPrice);
    }
}
