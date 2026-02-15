package com.jaypal.oms.order.domain.event;

import com.jaypal.oms.shared.kernel.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * OrderPlacedEvent
 *
 * Published when a customer places a new order.
 * Signals the start of the order saga.
 *
 * Flow:
 * 1. Order created in PENDING state
 * 2. OrderPlacedEvent published
 * 3. Inventory module listens and reserves stock
 * 4. Either StockReservedEvent or StockReservationFailedEvent published
 */
@Value
public class OrderPlacedEvent implements DomainEvent {
    private final UUID orderId;
    private final Map<String, Integer> skuQuantities;
    private final Instant occurredOn;

    public OrderPlacedEvent(UUID orderId, Map<String, Integer> skuQuantities) {
        this.orderId = orderId;
        this.skuQuantities = Map.copyOf(skuQuantities);
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}
