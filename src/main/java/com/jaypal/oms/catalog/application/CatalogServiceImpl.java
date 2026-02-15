package com.jaypal.oms.catalog.application;

import com.jaypal.oms.catalog.api.CatalogApi;
import com.jaypal.oms.catalog.api.ProductView;
import com.jaypal.oms.catalog.infrastructure.persistence.SpringDataProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Catalog Service Implementation
 *
 * Implements product queries with Redis caching for individual products.
 * List operations use pagination to limit result sets.
 * Cache is evicted when products are updated (future implementation).
 */
@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogApi {

    private final SpringDataProductRepository repository;

    public CatalogServiceImpl(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Get a single product by SKU - CACHED for 60 minutes
     *
     * Redis cache name: "products"
     * Cache key: the SKU value
     *
     * @param sku the product SKU
     * @return Optional containing the product if found
     */
    @Override
    @Cacheable(value = "products", key = "#sku")
    public Optional<ProductView> getProduct(String sku) {
        return repository.findById(sku)
                .map(p -> new ProductView(
                        p.getSku(),
                        p.getName(),
                        p.getPrice(),
                        p.isActive()));
    }

    /**
     * List all active products with pagination
     *
     * No caching on list operations to ensure consistency with database state.
     * Results are limited by Pageable to prevent large result sets.
     *
     * @param pageable pagination parameters (default size: 20)
     * @return Page of active products sorted by SKU
     */
    @Override
    public Page<ProductView> listActiveProducts(Pageable pageable) {
        return repository.findByActive(true, pageable)
                .map(p -> new ProductView(
                        p.getSku(),
                        p.getName(),
                        p.getPrice(),
                        p.isActive()));
    }

    /**
     * Clear product cache for a specific SKU
     *
     * Called when a product is updated. Future implementation will
     * trigger this via domain events.
     *
     * @param sku the product SKU
     */
    @CacheEvict(value = "products", key = "#sku")
    public void evictProductCache(String sku) {
        // Cache eviction is handled by annotation
    }

    /**
     * Clear all product cache entries
     *
     * Heavy operation - use sparingly. Called during bulk product updates.
     */
    @CacheEvict(value = "products", allEntries = true)
    public void evictAllProductCache() {
        // Cache eviction is handled by annotation
    }
}
