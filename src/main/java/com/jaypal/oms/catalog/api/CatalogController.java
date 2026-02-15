package com.jaypal.oms.catalog.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catalog REST API Controller
 *
 * Endpoints for product browsing with pagination and caching.
 *
 * Security: No authentication required (public read-only API)
 * - GET /catalog/products/{sku} : public, cached
 * - GET /catalog/products       : public, paginated
 */
@Slf4j
@RestController
@RequestMapping("/catalog/products")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogApi catalogApi;

    // ==================== Get Single Product ====================
    /**
     * Get a single product by SKU (cached)
     *
     * Security: Public - no authentication required
     *
     * @param sku the product SKU
     * @return product details or 404
     */
    @GetMapping("/{sku}")
    @Cacheable(value = "products", key = "#sku")
    public ResponseEntity<ProductView> getProduct(@PathVariable String sku) {
        log.debug("Product lookup requested for SKU: {}", sku);
        return catalogApi.getProduct(sku)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.debug("Product not found for SKU: {}", sku);
                    return ResponseEntity.notFound().build();
                });
    }

    // ==================== List Products with Pagination ====================
    /**
     * List all active products with pagination
     *
     * Security: Public - no authentication required
     *
     * @param page zero-indexed page number (default: 0)
     * @param size page size (default: 20, max: 100)
     * @return paginated product list
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ProductView>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Validate and clamp pagination parameters
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        log.debug("Product listing requested: page={}, size={}", pageNumber, pageSize);

        Page<ProductView> productPage = catalogApi.listActiveProducts(pageable);

        log.debug("Returning {} products from page {} (total elements: {})",
                productPage.getNumberOfElements(),
                pageNumber,
                productPage.getTotalElements());

        // Wrap Page content in a cleaner API DTO
        PagedResponse<ProductView> response = new PagedResponse<>(
                productPage.getContent(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    // ==================== Helper DTO ====================
    /**
     * Wrapper for paginated responses, avoids exposing internal Page fields directly
     */
    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements
    ) {}
}
