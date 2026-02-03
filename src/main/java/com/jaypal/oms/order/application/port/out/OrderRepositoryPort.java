package com.jaypal.oms.order.application.port.out;

import com.jaypal.oms.order.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting and loading orders.
 */
public interface OrderRepositoryPort {

    void save(Order order);

    Optional<Order> findById(UUID orderId);
}
