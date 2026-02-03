package com.jaypal.oms.order.application.usecase;

import com.jaypal.oms.order.application.port.out.InventoryPort;
import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.model.Order;
import com.jaypal.oms.order.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for cancelling an order.
 */
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final InventoryPort inventoryPort;

    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found: " + orderId)
                );

        order.cancel();

        inventoryPort.releaseStock(
                orderId,
                order.getItems().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                OrderItem::getSku,
                                OrderItem::getQuantity
                        ))
        );

        orderRepository.save(order);
    }

}
