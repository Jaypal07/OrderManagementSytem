package com.jaypal.oms.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Product JPA Entity
 *
 * Represents a product in the catalog with basic information.
 * Indexes are defined on commonly queried columns (active, sku).
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_active", columnList = "active", unique = false),
        @Index(name = "idx_product_sku", columnList = "sku", unique = true)
    }
)
public class ProductJpaEntity {

    @Id
    @Column(name = "sku", nullable = false, updatable = false)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected ProductJpaEntity() {
        // JPA default constructor
    }

    public ProductJpaEntity(String sku, String name, BigDecimal price, boolean active) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.active = active;
    }

    // ...existing code...

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }
}
