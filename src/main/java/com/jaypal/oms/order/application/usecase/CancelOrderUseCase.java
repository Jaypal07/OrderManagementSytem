package com.jaypal.oms.order.application.usecase;

import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cancel Order Use Case
 *
 * Cancels an existing order and initiates compensation saga.
 *
 * Flow:
 * 1. Load order from repository
 * 2. Transition to CANCELLED state
 * 3. Save order
 * 4. Publish OrderCancelledEvent (transactional)
 *
 * Event Flow (async, via listeners):
 * OrderCancelledEvent → InventoryModule → releases reserved stock
 *
 * Idempotency: Cancelling an already-cancelled order is no-op (safe)
 */
@Slf4j
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Cancel an order
     *
     * @param orderId the order to cancel
     * @throws IllegalArgumentException if order not found
     */
    @Transactional
    public void cancelOrder(UUID orderId) {
        log.info("Attempting to cancel order: {}", orderId);

        // Step 1: Load order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found for cancellation: {}", orderId);
                    return new IllegalArgumentException("Order not found: " + orderId);
                });

        log.debug("Order loaded (status: {}): {}", order.getStatus(), orderId);

        // Step 2: Attempt cancellation (may throw if in final state)
        try {
            order.cancel("Customer-initiated cancellation");
        } catch (Exception e) {
            log.warn("Cannot cancel order in state {}: {}", order.getStatus(), orderId);
            throw e;
        }

        // Step 3: Save cancelled order
        orderRepository.save(order);
        log.debug("Order marked as CANCELLED: {}", orderId);

        // Step 4: Publish cancellation event for inventory compensation
        // Event triggers stock release (idempotent if no stock was ever reserved)
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();

        log.info("Order cancelled successfully: {}", orderId);
    }
}
