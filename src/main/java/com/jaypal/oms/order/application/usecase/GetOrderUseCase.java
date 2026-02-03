package com.jaypal.oms.order.application.usecase;

import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.model.Order;

import java.util.UUID;

/**
 * Use case for retrieving an order.
 */
public class GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public GetOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found: " + orderId)
                );
    }
}
