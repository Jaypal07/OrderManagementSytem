package com.jaypal.oms.inventory.domain.model;

import com.jaypal.oms.shared.kernel.DomainException;

import java.util.Objects;

/**
 * Value object representing stock quantity.
 */
public final class StockLevel {

    private final int quantity;

    public StockLevel(int quantity) {
        if (quantity < 0) {
            throw new InvalidStockLevelException("Stock quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public StockLevel decreaseBy(int amount) {
        if (amount <= 0) {
            throw new InvalidStockLevelException("Decrease amount must be positive");
        }
        if (quantity < amount) {
            throw new InvalidStockLevelException("Insufficient stock");
        }
        return new StockLevel(quantity - amount);
    }

    public StockLevel increaseBy(int amount) {
        if (amount <= 0) {
            throw new InvalidStockLevelException("Increase amount must be positive");
        }
        return new StockLevel(quantity + amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockLevel)) return false;
        StockLevel that = (StockLevel) o;
        return quantity == that.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity);
    }

    private static class InvalidStockLevelException extends DomainException {
        private InvalidStockLevelException(String message) {
            super(message);
        }
    }
}
