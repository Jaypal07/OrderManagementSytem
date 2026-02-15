package com.jaypal.oms.catalog.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Catalog API - Port for product queries
 *
 * Provides access to product information with pagination support.
 * Single products are cached, list operations are paginated for performance.
 */
public interface CatalogApi {

    /**
     * Get a single product by SKU (cached)
     *
     * @param sku the product SKU
     * @return Optional containing the product if found
     */
    Optional<ProductView> getProduct(String sku);

    /**
     * List all active products with pagination
     *
     * @param pageable pagination parameters
     * @return Page of active products
     */
    Page<ProductView> listActiveProducts(Pageable pageable);
}
