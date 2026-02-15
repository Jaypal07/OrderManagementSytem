package com.jaypal.oms.order.domain.event;

import com.jaypal.oms.shared.kernel.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * OrderCancelledEvent
 *
 * Published when order is cancelled (either by customer or due to stock failure).
 *
 * Signals inventory to release previously reserved stock.
 */
@Value
public class OrderCancelledEvent implements DomainEvent {
    private final UUID orderId;
    private final String reason;
    private final Instant occurredOn;

    public OrderCancelledEvent(UUID orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}
