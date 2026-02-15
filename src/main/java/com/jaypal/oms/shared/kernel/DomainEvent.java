package com.jaypal.oms.shared.kernel;

import java.time.Instant;

/**
 * Marker interface for domain events.
 */
public interface DomainEvent {
    Instant occurredOn();
}
