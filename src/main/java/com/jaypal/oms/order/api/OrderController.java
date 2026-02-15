package com.jaypal.oms.order.api;

import com.jaypal.oms.order.application.port.in.PlaceOrderCommand;
import com.jaypal.oms.order.application.usecase.CancelOrderUseCase;
import com.jaypal.oms.order.application.usecase.GetOrderUseCase;
import com.jaypal.oms.order.application.usecase.PlaceOrderUseCase;
import com.jaypal.oms.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

/**
 * Order REST API Controller
 *
 * Endpoints for order management with role-based access control.
 *
 * Security:
 * - /orders POST (place order): Requires ROLE_USER
 * - /orders/{id} GET (view order): Requires ROLE_USER
 * - /orders/{id}/cancel POST (cancel order): Requires ROLE_ADMIN or ROLE_USER (owner)
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    /**
     * Place a new order
     *
     * Security: Requires ROLE_USER
     * @param request order items with quantities
     * @return orderId for tracking
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> placeOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {

        log.info("Order placement requested by user: {}", authentication.getName());

        UUID orderId = placeOrderUseCase.placeOrder(
                new PlaceOrderCommand(request.items()));

        log.info("Order placed successfully: {} by user: {}", orderId, authentication.getName());
        return ResponseEntity.status(201).body(Map.of("orderId", orderId));
    }

    /**
     * Get order details
     *
     * Security: Requires ROLE_USER
     * @param orderId the order to retrieve
     * @return order details
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getOrder(
            @PathVariable UUID orderId,
            Authentication authentication) {

        log.debug("Order details requested for order: {} by user: {}", orderId, authentication.getName());

        Order order = getOrderUseCase.getOrder(orderId);
        return ResponseEntity.ok(OrderApiMapper.toResponse(order));
    }

    /**
     * Cancel an existing order
     *
     * Security: Requires ROLE_ADMIN or ROLE_USER (own order)
     * @param orderId the order to cancel
     * @return 204 No Content
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(
            @PathVariable UUID orderId,
            Authentication authentication) {

        log.info("Order cancellation requested for order: {} by user: {}", orderId, authentication.getName());

        cancelOrderUseCase.cancelOrder(orderId);

        log.info("Order cancelled successfully: {} by user: {}", orderId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
