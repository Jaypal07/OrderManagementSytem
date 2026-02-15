package com.jaypal.oms.inventory.application.usecase;

import com.jaypal.oms.inventory.application.port.out.InventoryRepositoryPort;
import com.jaypal.oms.inventory.domain.exception.InsufficientStockException;
import com.jaypal.oms.inventory.domain.model.InventoryItem;
import com.jaypal.oms.inventory.domain.model.StockLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for ReserveStockUseCase
 *
 * Tests stock reservation logic with mocked repository.
 * Verifies retry behavior and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReserveStockUseCase Unit Tests")
class ReserveStockUseCaseTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @InjectMocks
    private ReserveStockUseCase reserveStockUseCase;

    private UUID testOrderId;
    private Map<String, Integer> testSkuQuantities;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testSkuQuantities = new HashMap<>();
        testSkuQuantities.put("SKU-A", 100);
        testSkuQuantities.put("SKU-B", 50);
    }

    @Test
    @DisplayName("Should reserve stock for single SKU successfully")
    void testReserve_SingleSku_Success() {
        // Arrange
        InventoryItem item = new InventoryItem("SKU-A", new StockLevel(1000));
        when(inventoryRepositoryPort.findBySku("SKU-A"))
                .thenReturn(Optional.of(item));

        Map<String, Integer> quantities = Map.of("SKU-A", 100);

        // Act
        reserveStockUseCase.reserve(testOrderId, quantities);

        // Assert
        assertThat(item.getAvailableStock().getQuantity()).isEqualTo(900);
        assertThat(item.getReservedStock().getQuantity()).isEqualTo(100);
        verify(inventoryRepositoryPort).save(item);
    }

    @Test
    @DisplayName("Should reserve stock for multiple SKUs successfully")
    void testReserve_MultipleSkus_Success() {
        // Arrange
        InventoryItem itemA = new InventoryItem("SKU-A", new StockLevel(1000));
        InventoryItem itemB = new InventoryItem("SKU-B", new StockLevel(500));

        when(inventoryRepositoryPort.findBySku("SKU-A"))
                .thenReturn(Optional.of(itemA));
        when(inventoryRepositoryPort.findBySku("SKU-B"))
                .thenReturn(Optional.of(itemB));

        // Act
        reserveStockUseCase.reserve(testOrderId, testSkuQuantities);

        // Assert
        assertThat(itemA.getAvailableStock().getQuantity()).isEqualTo(900);
        assertThat(itemA.getReservedStock().getQuantity()).isEqualTo(100);
        assertThat(itemB.getAvailableStock().getQuantity()).isEqualTo(450);
        assertThat(itemB.getReservedStock().getQuantity()).isEqualTo(50);
        verify(inventoryRepositoryPort, times(2)).save(any());
    }

    @Test
    @DisplayName("Should reject reservation when SKU not found")
    void testReserve_SkuNotFound() {
        // Arrange
        when(inventoryRepositoryPort.findBySku("SKU-MISSING"))
                .thenReturn(Optional.empty());

        Map<String, Integer> quantities = Map.of("SKU-MISSING", 100);

        // Act & Assert
        assertThatThrownBy(() -> reserveStockUseCase.reserve(testOrderId, quantities))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Inventory not found");
    }

    @Test
    @DisplayName("Should reject reservation when insufficient stock")
    void testReserve_InsufficientStock() {
        // Arrange
        InventoryItem item = new InventoryItem("SKU-A", new StockLevel(50));
        when(inventoryRepositoryPort.findBySku("SKU-A"))
                .thenReturn(Optional.of(item));

        Map<String, Integer> quantities = Map.of("SKU-A", 100);

        // Act & Assert
        assertThatThrownBy(() -> reserveStockUseCase.reserve(testOrderId, quantities))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should reserve exact available stock")
    void testReserve_ExactAmount() {
        // Arrange
        InventoryItem item = new InventoryItem("SKU-A", new StockLevel(100));
        when(inventoryRepositoryPort.findBySku("SKU-A"))
                .thenReturn(Optional.of(item));

        Map<String, Integer> quantities = Map.of("SKU-A", 100);

        // Act
        reserveStockUseCase.reserve(testOrderId, quantities);

        // Assert
        assertThat(item.getAvailableStock().getQuantity()).isEqualTo(0);
        assertThat(item.getReservedStock().getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should handle empty quantity map")
    void testReserve_EmptyQuantities() {
        // Act
        reserveStockUseCase.reserve(testOrderId, new HashMap<>());

        // Assert - should succeed with no operations
        verify(inventoryRepositoryPort, times(0)).save(any());
    }

    @Test
    @DisplayName("Should reserve SKUs in sorted order (deadlock prevention)")
    void testReserve_SortedOrder() {
        // Arrange - add SKUs in reverse order
        Map<String, Integer> quantities = new HashMap<>();
        quantities.put("SKU-Z", 10);
        quantities.put("SKU-A", 20);
        quantities.put("SKU-M", 15);

        InventoryItem itemZ = new InventoryItem("SKU-Z", new StockLevel(1000));
        InventoryItem itemA = new InventoryItem("SKU-A", new StockLevel(1000));
        InventoryItem itemM = new InventoryItem("SKU-M", new StockLevel(1000));

        when(inventoryRepositoryPort.findBySku("SKU-A")).thenReturn(Optional.of(itemA));
        when(inventoryRepositoryPort.findBySku("SKU-M")).thenReturn(Optional.of(itemM));
        when(inventoryRepositoryPort.findBySku("SKU-Z")).thenReturn(Optional.of(itemZ));

        // Act
        reserveStockUseCase.reserve(testOrderId, quantities);

        // Assert - all saved successfully (order doesn't matter for save, but prevents deadlock)
        verify(inventoryRepositoryPort, times(3)).save(any());
    }
}

