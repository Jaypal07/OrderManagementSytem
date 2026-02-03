package com.jaypal.oms.order.api;

import com.jaypal.oms.order.application.port.in.PlaceOrderCommand;
import com.jaypal.oms.order.application.usecase.CancelOrderUseCase;
import com.jaypal.oms.order.application.usecase.GetOrderUseCase;
import com.jaypal.oms.order.application.usecase.PlaceOrderUseCase;
import com.jaypal.oms.order.domain.model.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public OrderController(
            PlaceOrderUseCase placeOrderUseCase,
            CancelOrderUseCase cancelOrderUseCase,
            GetOrderUseCase getOrderUseCase) {

        this.placeOrderUseCase = placeOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestBody Map<String, Integer> skuQuantities) {

        UUID orderId = placeOrderUseCase.placeOrder(
                new PlaceOrderCommand(skuQuantities)
        );

        return ResponseEntity.ok(Map.of("orderId", orderId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable UUID orderId) {
        Order order = getOrderUseCase.getOrder(orderId);
        return ResponseEntity.ok(
                OrderApiMapper.toResponse(
                        getOrderUseCase.getOrder(orderId)
                )
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId) {
        cancelOrderUseCase.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
