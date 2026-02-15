package com.jaypal.oms.order.infrastructure.saga;

import com.jaypal.oms.inventory.application.usecase.ReleaseStockUseCase;
import com.jaypal.oms.inventory.application.usecase.ReserveStockUseCase;
import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.event.OrderPlacedEvent;
import com.jaypal.oms.order.domain.event.StockReservationFailedEvent;
import com.jaypal.oms.order.domain.event.StockReservedEvent;
import com.jaypal.oms.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Order Saga Orchestrator
 *
 * Implements the order creation saga using event-driven orchestration.
 *
 * Saga Pattern Flow:
 *
 * 1. Customer places order
 *    → PlaceOrderUseCase creates order (PENDING) and publishes OrderPlacedEvent
 *
 * 2. Saga receives OrderPlacedEvent
 *    → Calls InventoryModule.reserve() to reserve stock
 *    → Inventory module publishes StockReservedEvent or StockReservationFailedEvent
 *
 * 3a. Happy Path: StockReservedEvent received
 *     → Order transitions PENDING → CONFIRMED
 *     → Saga completes successfully
 *
 * 3b. Failure Path: StockReservationFailedEvent received
 *     → Order transitions PENDING → CANCELLED
 *     → Compensation: no inventory release needed (stock never reserved)
 *     → Customer notified of failure
 *
 * Key Characteristics:
 * - Orchestrator-driven (centralized coordination)
 * - Event-sourced (all state changes via events)
 * - Transactional (each handler in its own transaction)
 * - Idempotent (safe to replay events)
 * - Observable (logs all saga steps)
 * - Timeout handling (external scheduler for stuck orders)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final OrderRepositoryPort orderRepository;

    /**
     * Handle OrderPlacedEvent
     *
     * Entry point for the order saga.
     * Initiates inventory reservation for the order.
     *
     * If reservation fails, StockReservationFailedEvent will be published
     * by the inventory module and handled by handleStockReservationFailed().
     */
    @EventListener
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        UUID orderId = event.getOrderId();
        Map<String, Integer> skuQuantities = event.getSkuQuantities();

        log.info("Saga: OrderPlacedEvent received for order: {}", orderId);

        try {
            // Attempt stock reservation
            log.debug("Saga: Attempting to reserve stock for order: {} (skus: {})",
                    orderId, skuQuantities.keySet());

            reserveStockUseCase.reserve(orderId, skuQuantities);

            log.debug("Saga: Stock reservation succeeded for order: {}", orderId);
            // Inventory module will publish StockReservedEvent

        } catch (IllegalArgumentException e) {
            // SKU not found - publish failure event manually
            log.warn("Saga: Stock reservation failed (SKU not found) for order: {} - {}",
                    orderId, e.getMessage());
            handleStockReservationFailed(
                    new StockReservationFailedEvent(orderId, "SKU not found: " + e.getMessage())
            );
        } catch (Exception e) {
            // Other failures (insufficient stock, optimistic lock exhaustion, etc.)
            log.warn("Saga: Stock reservation failed for order: {} - {}", orderId, e.getMessage());
            handleStockReservationFailed(
                    new StockReservationFailedEvent(orderId, "Stock reservation failed: " + e.getMessage())
            );
        }
    }

    /**
     * Handle StockReservedEvent
     *
     * Called when inventory successfully reserves stock.
     * Confirms the order and completes the saga successfully.
     */
    @EventListener
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {
        UUID orderId = event.getOrderId();

        log.info("Saga: StockReservedEvent received for order: {}", orderId);

        try {
            // Load order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            log.debug("Saga: Order loaded (status: {}) for confirmation: {}",
                    order.getStatus(), orderId);

            // Transition to CONFIRMED
            order.confirm();
            orderRepository.save(order);

            log.info("Saga: Order confirmed successfully: {}", orderId);
            // Saga completes - order is now CONFIRMED and ready for fulfillment

        } catch (Exception e) {
            log.error("Saga: Failed to confirm order: {} - {}", orderId, e.getMessage(), e);
            // Order stuck in PENDING - external scheduler should identify and cleanup
        }
    }

    /**
     * Handle StockReservationFailedEvent
     *
     * Called when inventory fails to reserve stock.
     * Cancels the order and initiates compensation.
     *
     * Compensation: No inventory release needed since stock was never reserved.
     * Just need to mark order as CANCELLED and notify customer.
     */
    @EventListener
    @Transactional
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        UUID orderId = event.getOrderId();
        String reason = event.getReason();

        log.info("Saga: StockReservationFailedEvent received for order: {} - reason: {}",
                orderId, reason);

        try {
            // Load order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            log.debug("Saga: Order loaded (status: {}) for cancellation: {}",
                    order.getStatus(), orderId);

            // Transition to CANCELLED with compensation reason
            order.cancelIfNotAlreadyCancelled("Stock reservation failed: " + reason);
            orderRepository.save(order);

            log.info("Saga: Order cancelled due to stock failure: {} - reason: {}", orderId, reason);
            // Compensation complete - no inventory release needed (never reserved)
            // Customer notification would be handled by order service (out of scope)

        } catch (Exception e) {
            log.error("Saga: Failed to cancel order: {} - {}", orderId, e.getMessage(), e);
            // Order stuck in PENDING - external scheduler should identify and cleanup
        }
    }

    /**
     * Timeout handler (external scheduled job)
     *
     * For orders stuck in PENDING state for > X minutes:
     * - Likely cause: StockReservedEvent lost or listener crashed
     * - Action: Manually release stock and cancel order
     * - Trigger: External @Scheduled task checks DB every 5 minutes
     *
     * This is a safety net for saga failures and network issues.
     */
    public void handleOrderTimeout(UUID orderId) {
        log.warn("Saga: Order timeout detected (stuck in PENDING): {}", orderId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            if (order.getStatus() != com.jaypal.oms.order.domain.model.OrderStatus.PENDING) {
                log.debug("Saga: Order no longer in PENDING state, skipping timeout handling: {}", orderId);
                return;
            }

            // Release any reserved stock (idempotent)
            try {
                releaseStockUseCase.release(orderId, order.getItems().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                item -> item.getSku(),
                                item -> item.getQuantity()
                        )));
            } catch (Exception e) {
                log.debug("Saga: Stock release attempt (may not have been reserved): {} - {}",
                        orderId, e.getMessage());
            }

            // Cancel order
            order.cancelIfNotAlreadyCancelled("Order timeout - saga failure recovery");
            orderRepository.save(order);

            log.info("Saga: Order timeout recovery complete: {}", orderId);

        } catch (Exception e) {
            log.error("Saga: Failed to recover from timeout for order: {} - {}",
                    orderId, e.getMessage(), e);
        }
    }
}

