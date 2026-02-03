package com.jaypal.oms.order.api;

import com.jaypal.oms.order.domain.model.Order;

public class OrderApiMapper {

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(i -> new OrderItemResponse(
                                i.getSku(),
                                i.getQuantity(),
                                i.getUnitPrice()
                        ))
                        .toList()
        );
    }
}
