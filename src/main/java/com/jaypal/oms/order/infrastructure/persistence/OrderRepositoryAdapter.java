package com.jaypal.oms.order.infrastructure.persistence;

import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.model.Order;
import com.jaypal.oms.order.domain.model.OrderItem;
import com.jaypal.oms.order.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository orderRepository;
    private final SpringDataOrderItemRepository itemRepository;

    public OrderRepositoryAdapter(SpringDataOrderRepository orderRepository,
                                  SpringDataOrderItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity orderEntity =
                new OrderJpaEntity(
                        order.getOrderId(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                );

        orderRepository.save(orderEntity);

        itemRepository.deleteAll(
                itemRepository.findByOrderId(order.getOrderId())
        );

        List<OrderItemJpaEntity> items =
                order.getItems().stream()
                        .map(i -> new OrderItemJpaEntity(
                                order.getOrderId(),
                                i.getSku(),
                                i.getQuantity(),
                                i.getUnitPrice()
                        ))
                        .toList();

        itemRepository.saveAll(items);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(orderEntity -> {
                    List<OrderItemJpaEntity> items =
                            itemRepository.findByOrderId(orderId);

                    List<OrderItem> domainItems =
                            items.stream()
                                    .map(i -> new OrderItem(
                                            i.getSku(),
                                            i.getQuantity(),
                                            i.getUnitPrice()
                                    ))
                                    .toList();

                    Order order = new Order(orderId, domainItems);
                    if (OrderStatus.valueOf(orderEntity.getStatus())
                            == OrderStatus.CANCELLED) {
                        order.cancel();
                    }
                    if (OrderStatus.valueOf(orderEntity.getStatus())
                            == OrderStatus.CONFIRMED) {
                        order.confirm();
                    }

                    return order;
                });
    }
}