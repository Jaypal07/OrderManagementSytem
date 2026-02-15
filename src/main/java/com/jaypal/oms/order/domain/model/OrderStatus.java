package com.jaypal.oms.order.domain.model;

/**
 * Represents the lifecycle state of an Order.
 * Transitions are enforced in domain logic.
 *
 * State Machine:
 * CREATED → PENDING → CONFIRMED → COMPLETED
 *                  ↓
 *              CANCELLED (compensation)
 */
public enum OrderStatus {

    /**
     * Order just created, not yet submitted to inventory
     */
    CREATED,

    /**
     * Order submitted to inventory for stock reservation
     * Waiting for StockReservedEvent or StockReservationFailedEvent
     */
    PENDING,

    /**
     * Stock successfully reserved, order confirmed
     */
    CONFIRMED,

    /**
     * Order completed and fulfilled
     */
    COMPLETED,

    /**
     * Order cancelled (either by customer or due to stock failure)
     * Compensation: inventory stock released
     */
    CANCELLED
}
