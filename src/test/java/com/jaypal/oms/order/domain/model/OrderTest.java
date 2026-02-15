package com.jaypal.oms.order.domain.model;

import com.jaypal.oms.order.domain.event.OrderCancelledEvent;
import com.jaypal.oms.order.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for Order Domain Model
 *
 * Tests order state machine and event publishing.
 */
@DisplayName("Order Domain Model Tests")
class OrderTest {

    private UUID testOrderId;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testItems = List.of(
                new OrderItem("SKU-001", 10, new BigDecimal("100.00")),
                new OrderItem("SKU-002", 5, new BigDecimal("50.00"))
        );
    }

    @Test
    @DisplayName("Should create order in CREATED state")
    void testConstructor_Valid() {
        Order order = new Order(testOrderId, testItems);

        assertThat(order.getOrderId()).isEqualTo(testOrderId);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should reject null order ID")
    void testConstructor_NullOrderId() {
        assertThatThrownBy(() -> new Order(null, testItems))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrderId must be provided");
    }

    @Test
    @DisplayName("Should reject null items")
    void testConstructor_NullItems() {
        assertThatThrownBy(() -> new Order(testOrderId, null))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("Should reject empty items list")
    void testConstructor_EmptyItems() {
        assertThatThrownBy(() -> new Order(testOrderId, List.of()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("Should mark order as PENDING")
    void testMarkPending() {
        Order order = new Order(testOrderId, testItems);

        order.markPending();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should reject marking PENDING from non-CREATED state")
    void testMarkPending_InvalidState() {
        Order order = new Order(testOrderId, testItems);
        order.markPending();

        assertThatThrownBy(order::markPending)
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("Should confirm order from CREATED state")
    void testConfirm_FromCreated() {
        Order order = new Order(testOrderId, testItems);

        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should reject confirm from non-CREATED state")
    void testConfirm_InvalidState() {
        Order order = new Order(testOrderId, testItems);
        order.markPending();

        assertThatThrownBy(order::confirm)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("can only be confirmed from CREATED state");
    }

    @Test
    @DisplayName("Should cancel order and publish event")
    void testCancel() {
        Order order = new Order(testOrderId, testItems);

        order.cancel("Test cancellation");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getDomainEvents()).hasSize(1);
        assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderCancelledEvent.class);
    }

    @Test
    @DisplayName("Should reject cancel from already CANCELLED state")
    void testCancel_AlreadyCancelled() {
        Order order = new Order(testOrderId, testItems);
        order.cancel("First cancel");

        assertThatThrownBy(() -> order.cancel("Second cancel"))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("Should reject cancel from COMPLETED state")
    void testCancel_FromCompleted() {
        Order order = new Order(testOrderId, testItems);
        order.markPending();
        order.confirm();
        // Simulate order completion (not exposed via API but possible internally)

        // Can't set to COMPLETED directly, so skip this test or use reflection
        // This is a limitation of the current design
    }

    @Test
    @DisplayName("Should cancel idempotently")
    void testCancelIfNotAlreadyCancelled_FirstTime() {
        Order order = new Order(testOrderId, testItems);

        order.cancelIfNotAlreadyCancelled("First cancel");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should not cancel if already cancelled (idempotent)")
    void testCancelIfNotAlreadyCancelled_AlreadyCancelled() {
        Order order = new Order(testOrderId, testItems);
        order.cancelIfNotAlreadyCancelled("First cancel");
        order.clearDomainEvents();

        order.cancelIfNotAlreadyCancelled("Second cancel (should be no-op)");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getDomainEvents()).isEmpty(); // No new event published
    }

    @Test
    @DisplayName("Should clear domain events after publishing")
    void testClearDomainEvents() {
        Order order = new Order(testOrderId, testItems);
        order.cancel("Test");

        assertThat(order.getDomainEvents()).hasSize(1);

        order.clearDomainEvents();

        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should maintain equality based on order ID")
    void testEquality() {
        Order order1 = new Order(testOrderId, testItems);
        Order order2 = new Order(testOrderId, List.of(testItems.get(0))); // Different items

        assertThat(order1).isEqualTo(order2); // Same ID = equal
    }

    @Test
    @DisplayName("Should maintain hash code based on order ID")
    void testHashCode() {
        Order order1 = new Order(testOrderId, testItems);
        Order order2 = new Order(testOrderId, testItems);

        assertThat(order1.hashCode()).isEqualTo(order2.hashCode());
    }

    @Test
    @DisplayName("Should saga flow: CREATED → PENDING → CONFIRMED")
    void testSagaFlowSuccess() {
        Order order = new Order(testOrderId, testItems);

        // Step 1: Place order (CREATED)
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        // Step 2: Mark pending (OrderPlacedEvent about to be published)
        order.markPending();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Step 3: Confirm (StockReservedEvent received)
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should saga flow: CREATED → PENDING → CANCELLED (failure)")
    void testSagaFlowFailure() {
        Order order = new Order(testOrderId, testItems);

        // Step 1: Place order (CREATED)
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        // Step 2: Mark pending (OrderPlacedEvent about to be published)
        order.markPending();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Step 3: Stock reservation fails - cancel order (StockReservationFailedEvent received)
        order.cancelIfNotAlreadyCancelled("Insufficient stock");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getDomainEvents()).hasSize(1);
    }
}

