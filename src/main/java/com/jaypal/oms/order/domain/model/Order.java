package com.jaypal.oms.order.domain.model;

import com.jaypal.oms.order.domain.exception.InvalidOrderStateException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for Order.
 */
public class Order {

    private final UUID orderId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Instant createdAt;

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
     * Confirms the order.
     * Allowed only from CREATED state.
     */
    public void confirm() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "Order can only be confirmed from CREATED state"
            );
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Cancels the order.
     * Allowed only if not already cancelled.
     */
    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return orderId.equals(order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
