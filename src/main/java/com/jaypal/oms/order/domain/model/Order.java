package com.jaypal.oms.order.domain.model;

import com.jaypal.oms.order.domain.event.OrderCancelledEvent;
import com.jaypal.oms.order.domain.exception.InvalidOrderStateException;
import com.jaypal.oms.shared.kernel.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root for Order
 *
 * Manages order lifecycle with state transitions:
 * CREATED → PENDING → CONFIRMED → COMPLETED
 *                  ↓
 *              CANCELLED (compensation)
 *
 * Publishes domain events for saga coordination.
 */
public class Order {

    private final UUID orderId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Order(UUID orderId, List<OrderItem> items) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId must be provided");
        }
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderStateException("Order must contain at least one item");
        }

        this.orderId = orderId;
        this.items = List.copyOf(items);
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public UUID getOrderId() {
        return orderId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Get all domain events pending publication.
     * Events are cleared after retrieval to prevent double-publishing.
     */
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    /**
     * Clear domain events after publication.
     * Called by infrastructure layer after publishing events.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Confirms the order.
     * Allowed only from CREATED state.
     * Called when StockReservedEvent is received.
     */
    public void confirm() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "Order can only be confirmed from CREATED state, current: " + status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Transitions order to PENDING state.
     * Internal state only - not reflected in API.
     * Used to indicate stock reservation is in progress.
     */
    public void markPending() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "Order can only be marked pending from CREATED state");
        }
        this.status = OrderStatus.PENDING;
    }

    /**
     * Cancels the order and publishes OrderCancelledEvent.
     * Allowed if order is not already in final state.
     *
     * @param reason cancellation reason for audit trail
     */
    public void cancel(String reason) {
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }
        if (status == OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException("Cannot cancel a completed order");
        }

        this.status = OrderStatus.CANCELLED;

        // Publish event for compensation (inventory release)
        domainEvents.add(new OrderCancelledEvent(orderId, reason));
    }

    /**
     * Cancels without throwing if already cancelled.
     * Used for idempotent compensation.
     */
    public void cancelIfNotAlreadyCancelled(String reason) {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.COMPLETED) {
            return; // Idempotent: no-op
        }
        cancel(reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Order))
            return false;
        Order order = (Order) o;
        return orderId.equals(order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}


