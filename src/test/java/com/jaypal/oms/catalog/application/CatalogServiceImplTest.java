package com.jaypal.oms.catalog.application;

import com.jaypal.oms.catalog.api.ProductView;
import com.jaypal.oms.catalog.infrastructure.persistence.ProductJpaEntity;
import com.jaypal.oms.catalog.infrastructure.persistence.SpringDataProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for CatalogServiceImpl
 *
 * Tests the business logic of product retrieval and listing.
 * Mocks repository layer for isolated testing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Catalog Service Unit Tests")
class CatalogServiceImplTest {

    @Mock
    private SpringDataProductRepository repository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private ProductJpaEntity testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new ProductJpaEntity(
                "SKU-123",
                "Test Laptop",
                new BigDecimal("1299.99"),
                true
        );
    }

    @Test
    @DisplayName("Should retrieve product by SKU successfully")
    void testGetProductBySku_Success() {
        // Arrange
        when(repository.findById("SKU-123")).thenReturn(Optional.of(testProduct));

        // Act
        Optional<ProductView> result = catalogService.getProduct("SKU-123");

        // Assert
        assertThat(result)
                .isPresent()
                .contains(new ProductView("SKU-123", "Test Laptop", new BigDecimal("1299.99"), true));
        verify(repository).findById("SKU-123");
    }

    @Test
    @DisplayName("Should return empty Optional when product not found")
    void testGetProductBySku_NotFound() {
        // Arrange
        when(repository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        // Act
        Optional<ProductView> result = catalogService.getProduct("NONEXISTENT");

        // Assert
        assertThat(result).isEmpty();
        verify(repository).findById("NONEXISTENT");
    }

    @Test
    @DisplayName("Should list active products with pagination")
    void testListActiveProducts_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductJpaEntity> mockPage = new PageImpl<>(
                List.of(testProduct),
                pageable,
                1
        );
        when(repository.findByActive(true, pageable)).thenReturn(mockPage);

        // Act
        Page<ProductView> result = catalogService.listActiveProducts(pageable);

        // Assert
        assertThat(result)
                .hasSize(1)
                .isNotEmpty();
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU-123");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        verify(repository).findByActive(true, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no active products exist")
    void testListActiveProducts_Empty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductJpaEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findByActive(true, pageable)).thenReturn(emptyPage);

        // Act
        Page<ProductView> result = catalogService.listActiveProducts(pageable);

        // Assert
        assertThat(result)
                .isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(repository).findByActive(true, pageable);
    }

    @Test
    @DisplayName("Should list multiple pages of active products")
    void testListActiveProducts_MultiPage() {
        // Arrange
        ProductJpaEntity product2 = new ProductJpaEntity(
                "SKU-456",
                "Test Mouse",
                new BigDecimal("29.99"),
                true
        );
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProductJpaEntity> mockPage = new PageImpl<>(
                List.of(testProduct),
                pageable,
                2 // Total: 2 products, but page size is 1
        );
        when(repository.findByActive(true, pageable)).thenReturn(mockPage);

        // Act
        Page<ProductView> result = catalogService.listActiveProducts(pageable);

        // Assert
        assertThat(result)
                .hasSize(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should map product entity to ProductView correctly")
    void testProductEntityToViewMapping() {
        // Arrange
        when(repository.findById("SKU-123")).thenReturn(Optional.of(testProduct));

        // Act
        ProductView result = catalogService.getProduct("SKU-123").orElseThrow();

        // Assert
        assertThat(result.sku()).isEqualTo("SKU-123");
        assertThat(result.name()).isEqualTo("Test Laptop");
        assertThat(result.price()).isEqualTo(new BigDecimal("1299.99"));
        assertThat(result.active()).isTrue();
    }
}

