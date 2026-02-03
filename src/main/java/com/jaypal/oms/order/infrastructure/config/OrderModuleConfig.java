package com.jaypal.oms.order.infrastructure.config;

import com.jaypal.oms.inventory.application.usecase.ReleaseStockUseCase;
import com.jaypal.oms.inventory.application.usecase.ReserveStockUseCase;
import com.jaypal.oms.order.application.port.out.InventoryPort;
import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.application.usecase.CancelOrderUseCase;
import com.jaypal.oms.order.application.usecase.GetOrderUseCase;
import com.jaypal.oms.order.application.usecase.PlaceOrderUseCase;
import com.jaypal.oms.order.infrastructure.persistence.OrderRepositoryAdapter;
import com.jaypal.oms.order.infrastructure.persistence.SpringDataOrderItemRepository;
import com.jaypal.oms.order.infrastructure.persistence.SpringDataOrderRepository;
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
    InventoryPort inventoryPort(
            ReserveStockUseCase reserveStockUseCase,
            ReleaseStockUseCase releaseStockUseCase) {

        return new InventoryPort() {
            @Override
            public void reserveStock(
                    java.util.UUID orderId,
                    java.util.Map<String, Integer> skuQuantities) {

                reserveStockUseCase.reserve(
                        new com.jaypal.oms.inventory.application.port.in.ReserveStockCommand(
                                orderId,
                                skuQuantities
                        )
                );
            }

            @Override
            public void releaseStock(
                    java.util.UUID orderId,
                    java.util.Map<String, Integer> skuQuantities) {

                releaseStockUseCase.release(skuQuantities);
            }
        };
    }


    @Bean
    @Transactional
    PlaceOrderUseCase placeOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            InventoryPort inventoryPort) {

        return new PlaceOrderUseCase(orderRepositoryPort, inventoryPort);
    }

    @Bean
    @Transactional
    CancelOrderUseCase cancelOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            InventoryPort inventoryPort) {

        return new CancelOrderUseCase(orderRepositoryPort, inventoryPort);
    }

    @Bean
    GetOrderUseCase getOrderUseCase(
            OrderRepositoryPort orderRepositoryPort) {

        return new GetOrderUseCase(orderRepositoryPort);
    }
}
