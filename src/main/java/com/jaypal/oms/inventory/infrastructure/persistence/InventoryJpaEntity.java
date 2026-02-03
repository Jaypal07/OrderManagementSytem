package com.jaypal.oms.inventory.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class InventoryJpaEntity {

    @Id
    @Column(name = "sku", nullable = false, updatable = false)
    private String sku;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected InventoryJpaEntity() {
        // JPA
    }

    public InventoryJpaEntity(String sku, int availableStock, int reservedStock) {
        this.sku = sku;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
    }

    public String getSku() {
        return sku;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
    }
}
