package com.jaypal.oms.inventory.domain.model;

import com.jaypal.oms.inventory.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for InventoryItem Domain Model
 *
 * Tests the business logic of stock reservation and release.
 * Ensures domain invariants are maintained (no negative stock).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryItem Domain Model Tests")
class InventoryItemTest {

    private InventoryItem inventory;
    private static final String TEST_SKU = "SKU-TEST-001";

    @BeforeEach
    void setUp() {
        inventory = new InventoryItem(
                TEST_SKU,
                new StockLevel(1000)
        );
    }

    @Test
    @DisplayName("Should construct InventoryItem with valid parameters")
    void testConstructor_Valid() {
        assertThat(inventory.getSku()).isEqualTo(TEST_SKU);
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(1000);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject null SKU")
    void testConstructor_NullSku() {
        assertThatThrownBy(() ->
            new InventoryItem(null, new StockLevel(100))
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SKU must be provided");
    }

    @Test
    @DisplayName("Should reject blank SKU")
    void testConstructor_BlankSku() {
        assertThatThrownBy(() ->
            new InventoryItem("   ", new StockLevel(100))
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SKU must be provided");
    }

    @Test
    @DisplayName("Should reject null stock level")
    void testConstructor_NullStockLevel() {
        assertThatThrownBy(() ->
            new InventoryItem(TEST_SKU, null)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Available stock must be provided");
    }

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserve_Success() {
        // Act
        inventory.reserve(500);

        // Assert
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(500);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should reserve exact available stock")
    void testReserve_ExactAmount() {
        // Act
        inventory.reserve(1000);

        // Assert
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(0);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should reject reservation exceeding available stock")
    void testReserve_InsufficientStock() {
        // Act & Assert
        assertThatThrownBy(() -> inventory.reserve(1001))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should reject zero reservation")
    void testReserve_ZeroQuantity() {
        // This should fail at StockLevel level
        assertThatThrownBy(() -> inventory.reserve(0))
        .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should reject negative reservation")
    void testReserve_NegativeQuantity() {
        assertThatThrownBy(() -> inventory.reserve(-10))
        .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should release reserved stock successfully")
    void testRelease_Success() {
        // Arrange
        inventory.reserve(500);

        // Act
        inventory.release(300);

        // Assert
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(800);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should release all reserved stock")
    void testRelease_AllReserved() {
        // Arrange
        inventory.reserve(500);

        // Act
        inventory.release(500);

        // Assert
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(1000);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject release exceeding reserved stock")
    void testRelease_ExceedsReserved() {
        // Arrange
        inventory.reserve(500);

        // Act & Assert
        assertThatThrownBy(() -> inventory.release(501))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot release more stock than reserved");
    }

    @Test
    @DisplayName("Should reject release when nothing reserved")
    void testRelease_NothingReserved() {
        // Act & Assert
        assertThatThrownBy(() -> inventory.release(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot release more stock than reserved");
    }

    @Test
    @DisplayName("Should handle multiple reserve and release operations")
    void testMultipleOperations() {
        // Reserve
        inventory.reserve(300);
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(700);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(300);

        // Release partial
        inventory.release(200);
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(900);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(100);

        // Reserve more
        inventory.reserve(200);
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(700);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(300);

        // Release all
        inventory.release(300);
        assertThat(inventory.getAvailableStock().getQuantity()).isEqualTo(1000);
        assertThat(inventory.getReservedStock().getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should maintain invariant: available + reserved = total")
    void testInvariant_TotalStock() {
        int initialTotal = inventory.getAvailableStock().getQuantity() +
                           inventory.getReservedStock().getQuantity();

        inventory.reserve(300);
        int afterReserve = inventory.getAvailableStock().getQuantity() +
                          inventory.getReservedStock().getQuantity();

        inventory.release(200);
        int afterRelease = inventory.getAvailableStock().getQuantity() +
                          inventory.getReservedStock().getQuantity();

        assertThat(initialTotal).isEqualTo(1000);
        assertThat(afterReserve).isEqualTo(1000);
        assertThat(afterRelease).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should be equal based on SKU only")
    void testEquality() {
        InventoryItem same = new InventoryItem(TEST_SKU, new StockLevel(500));
        InventoryItem different = new InventoryItem("SKU-OTHER", new StockLevel(1000));

        assertThat(inventory).isEqualTo(same);
        assertThat(inventory).isNotEqualTo(different);
    }

    @Test
    @DisplayName("Should have consistent hash code")
    void testHashCode() {
        InventoryItem same = new InventoryItem(TEST_SKU, new StockLevel(500));
        assertThat(inventory.hashCode()).isEqualTo(same.hashCode());
    }
}

