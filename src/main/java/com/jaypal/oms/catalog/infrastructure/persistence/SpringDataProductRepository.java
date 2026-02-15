package com.jaypal.oms.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA Repository for Product persistence
 *
 * Provides database access for product entities with pagination support.
 * Indexes are required on 'active' and 'sku' columns for optimal query performance.
 */
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, String> {

    /**
     * Find all active products with pagination
     *
     * Uses index on 'active' column for efficient filtering.
     *
     * @param active filter by active status
     * @param pageable pagination parameters
     * @return Page of active products
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.active = :active ORDER BY p.sku ASC")
    Page<ProductJpaEntity> findByActive(boolean active, Pageable pageable);
}
