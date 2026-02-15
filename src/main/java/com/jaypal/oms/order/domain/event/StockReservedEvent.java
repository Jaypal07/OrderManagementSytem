package com.jaypal.oms.order.domain.event;

import com.jaypal.oms.shared.kernel.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * StockReservedEvent
 *
 * Published by inventory module when stock reservation succeeds.
 * Signals that all required stock has been reserved.
 *
 * Causes order to transition from PENDING → CONFIRMED
 */
@Value
public class StockReservedEvent implements DomainEvent {
    private final UUID orderId;
    private final Map<String, Integer> reservedQuantities;
    private final Instant occurredOn;

    public StockReservedEvent(UUID orderId, Map<String, Integer> reservedQuantities) {
        this.orderId = orderId;
        this.reservedQuantities = Map.copyOf(reservedQuantities);
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}

