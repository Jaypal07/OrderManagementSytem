package com.jaypal.oms.order.domain.event;

import com.jaypal.oms.shared.kernel.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * StockReservationFailedEvent
 *
 * Published by inventory module when stock reservation fails.
 * Reasons: insufficient stock, SKU not found, optimistic lock exhaustion, etc.
 *
 * Triggers compensation:
 * 1. Order transitions to CANCELLED
 * 2. No inventory release needed (reservation never succeeded)
 * 3. Customer notified of failure
 */
@Value
public class StockReservationFailedEvent implements DomainEvent {
    private final UUID orderId;
    private final String reason;
    private final Instant occurredOn;

    public StockReservationFailedEvent(UUID orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}

