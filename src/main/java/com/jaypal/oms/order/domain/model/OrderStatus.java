package com.jaypal.oms.order.domain.model;

/**
 * Represents the lifecycle state of an Order.
 * Transitions are enforced in domain logic.
 */
public enum OrderStatus {

    CREATED,
    CONFIRMED,
    CANCELLED
}
