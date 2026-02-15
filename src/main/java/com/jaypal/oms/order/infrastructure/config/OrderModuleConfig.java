package com.jaypal.oms.order.infrastructure.config;

import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.application.usecase.CancelOrderUseCase;
import com.jaypal.oms.order.application.usecase.GetOrderUseCase;
import com.jaypal.oms.order.application.usecase.PlaceOrderUseCase;
import com.jaypal.oms.order.infrastructure.persistence.OrderRepositoryAdapter;
import com.jaypal.oms.order.infrastructure.persistence.SpringDataOrderItemRepository;
import com.jaypal.oms.order.infrastructure.persistence.SpringDataOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class OrderModuleConfig {

    @Bean
    OrderRepositoryPort orderRepositoryPort(
            SpringDataOrderRepository orderRepository,
            SpringDataOrderItemRepository itemRepository) {
        return new OrderRepositoryAdapter(orderRepository, itemRepository);
    }

    @Bean
    @Transactional
    PlaceOrderUseCase placeOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            com.jaypal.oms.order.application.port.out.CatalogPort catalogPort,
            ApplicationEventPublisher eventPublisher) {

        return new PlaceOrderUseCase(orderRepositoryPort, catalogPort, eventPublisher);
    }

    @Bean
    @Transactional
    CancelOrderUseCase cancelOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            ApplicationEventPublisher eventPublisher) {

        return new CancelOrderUseCase(orderRepositoryPort, eventPublisher);
    }

    @Bean
    GetOrderUseCase getOrderUseCase(
            OrderRepositoryPort orderRepositoryPort) {

        return new GetOrderUseCase(orderRepositoryPort);
    }
}
