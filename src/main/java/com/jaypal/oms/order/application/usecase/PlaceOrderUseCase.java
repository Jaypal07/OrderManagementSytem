package com.jaypal.oms.order.application.usecase;

import com.jaypal.oms.order.application.port.in.PlaceOrderCommand;
import com.jaypal.oms.order.application.port.out.InventoryPort;
import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.model.Order;
import com.jaypal.oms.order.domain.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Use case for placing an order.
 * Coordinates order creation and inventory reservation.
 */
public class PlaceOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final InventoryPort inventoryPort;

    public PlaceOrderUseCase(OrderRepositoryPort orderRepository,
                             InventoryPort inventoryPort) {
        this.orderRepository = orderRepository;
        this.inventoryPort = inventoryPort;
    }

    public UUID placeOrder(PlaceOrderCommand command) {
        UUID orderId = UUID.randomUUID();

        List<OrderItem> items = toOrderItems(command.getSkuQuantities());

        Order order = new Order(orderId, items);

        inventoryPort.reserveStock(orderId, command.getSkuQuantities());

        order.confirm();

        orderRepository.save(order);

        return orderId;
    }

    private List<OrderItem> toOrderItems(Map<String, Integer> skuQuantities) {
        return skuQuantities.entrySet().stream()
                .map(entry ->
                        new OrderItem(
                                entry.getKey(),
                                entry.getValue(),
                                resolvePrice(entry.getKey())
                        )
                )
                .toList();
    }

    /**
     * Placeholder for pricing resolution.
     * This will later be delegated to Catalog via a port.
     */
    private BigDecimal resolvePrice(String sku) {
        return BigDecimal.TEN;
    }
}
