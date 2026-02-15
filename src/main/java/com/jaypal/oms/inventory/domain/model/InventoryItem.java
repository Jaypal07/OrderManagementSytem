package com.jaypal.oms.inventory.domain.model;

import com.jaypal.oms.inventory.domain.exception.InsufficientStockException;

import java.util.Objects;

/**
 * Aggregate root representing inventory for a single SKU.
 */
public class InventoryItem {

    private final String sku;
    private StockLevel availableStock;
    private StockLevel reservedStock;

    public InventoryItem(String sku, StockLevel availableStock) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must be provided");
        }
        if (availableStock == null) {
            throw new IllegalArgumentException("Available stock must be provided");
        }

        this.sku = sku;
        this.availableStock = availableStock;
        this.reservedStock = new StockLevel(0);
    }

    public String getSku() {
        return sku;
    }

    public StockLevel getAvailableStock() {
        return availableStock;
    }

    public StockLevel getReservedStock() {
        return reservedStock;
    }

    /**
     * Public setter for use by persistence adapter.
     * Used during domain object reconstruction from database.
     *
     * @param reservedStock the reserved stock level
     */
    public void setReservedStock(StockLevel reservedStock) {
        this.reservedStock = reservedStock;
    }

    /**
     * Reserves stock for an order.
     */
    public void reserve(int quantity) {
        if (availableStock.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for SKU: " + sku
            );
        }

        this.availableStock = availableStock.decreaseBy(quantity);
        this.reservedStock = reservedStock.increaseBy(quantity);
    }

    /**
     * Releases previously reserved stock.
     */
    public void release(int quantity) {
        if (reservedStock.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Cannot release more stock than reserved for SKU: " + sku
            );
        }

        this.reservedStock = reservedStock.decreaseBy(quantity);
        this.availableStock = availableStock.increaseBy(quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItem)) return false;
        InventoryItem that = (InventoryItem) o;
        return sku.equals(that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }
}
