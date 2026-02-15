package com.jaypal.oms.order.application.usecase;

import com.jaypal.oms.order.application.port.in.PlaceOrderCommand;
import com.jaypal.oms.order.application.port.out.CatalogPort;
import com.jaypal.oms.order.application.port.out.InventoryPort;
import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.event.OrderPlacedEvent;
import com.jaypal.oms.order.domain.model.Order;
import com.jaypal.oms.order.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Place Order Use Case
 *
 * Orchestrates order creation and inventory reservation via saga pattern.
 *
 * Flow:
 * 1. Create order in CREATED state
 * 2. Validate products exist and have prices
 * 3. Save order to repository
 * 4. Publish OrderPlacedEvent (transactional)
 * 5. Mark order as PENDING
 * 6. Return order ID
 *
 * Event Flow (async, via listeners):
 * OrderPlacedEvent → InventoryModule → StockReservedEvent (success) or StockReservationFailedEvent (failure)
 *
 * Idempotency: Uses order ID to prevent double-processing
 */
@Slf4j
@RequiredArgsConstructor
public class PlaceOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CatalogPort catalogPort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Place an order and initiate inventory saga
     *
     * @param command PlaceOrderCommand with SKU quantities
     * @return orderId for tracking
     * @throws IllegalArgumentException if product not found
     */
    @Transactional
    public UUID placeOrder(PlaceOrderCommand command) {
        UUID orderId = UUID.randomUUID();

        log.info("Placing order: {} with {} items", orderId, command.getSkuQuantities().size());

        // Step 1: Validate and create order
        List<OrderItem> items = toOrderItems(command.getSkuQuantities());
        Order order = new Order(orderId, items);

        // Step 2: Save order in CREATED state
        orderRepository.save(order);
        log.debug("Order saved with CREATED status: {}", orderId);

        // Step 3: Transition to PENDING and mark for saga processing
        order.markPending();
        orderRepository.save(order);

        // Step 4: Publish OrderPlacedEvent within transaction
        // Event is published at commit time via @TransactionalEventListener in saga coordinator
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, command.getSkuQuantities());
        eventPublisher.publishEvent(event);

        log.info("Order placed successfully: {} (status: PENDING, waiting for stock reservation)", orderId);
        return orderId;
    }

    /**
     * Convert SKU quantities to OrderItems with pricing
     */
    private List<OrderItem> toOrderItems(Map<String, Integer> skuQuantities) {
        return skuQuantities.entrySet().stream()
                .map(entry -> new OrderItem(
                        entry.getKey(),
                        entry.getValue(),
                        resolvePrice(entry.getKey())))
                .toList();
    }

    /**
     * Resolve product price from catalog
     */
    private BigDecimal resolvePrice(String sku) {
        return catalogPort.getPrice(sku)
                .orElseThrow(() -> {
                    log.warn("Product not found for SKU: {}", sku);
                    return new IllegalArgumentException("Product not found: " + sku);
                });
    }
}
