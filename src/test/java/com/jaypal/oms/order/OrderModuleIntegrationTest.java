package com.jaypal.oms.order;

import com.jaypal.oms.order.application.port.in.PlaceOrderCommand;
import com.jaypal.oms.order.application.usecase.CancelOrderUseCase;
import com.jaypal.oms.order.application.usecase.PlaceOrderUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderModuleIntegrationTest {

    @Autowired
    private PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private CancelOrderUseCase cancelOrderUseCase;

    @Test
    void shouldPlaceOrderSuccessfully() {
        PlaceOrderCommand command = new PlaceOrderCommand(
                Map.of("SKU-123", 1));
        UUID orderId = placeOrderUseCase.placeOrder(command);
        assertNotNull(orderId);
    }

    @Test
    void shouldFailWhenProductNotFound() {
        PlaceOrderCommand command = new PlaceOrderCommand(
                Map.of("INVALID-SKU", 1));
        assertThrows(IllegalArgumentException.class, () -> placeOrderUseCase.placeOrder(command));
    }

    @Test
    void shouldFailWhenInsufficientStock() {
        PlaceOrderCommand command = new PlaceOrderCommand(
                Map.of("SKU-123", 1000));
        assertThrows(RuntimeException.class, () -> placeOrderUseCase.placeOrder(command));
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        PlaceOrderCommand command = new PlaceOrderCommand(
                Map.of("SKU-456", 1));
        UUID orderId = placeOrderUseCase.placeOrder(command);

        assertDoesNotThrow(() -> cancelOrderUseCase.cancelOrder(orderId));
    }

    @Test
    void shouldFailToCancelUnknownOrder() {
        assertThrows(IllegalArgumentException.class, () -> cancelOrderUseCase.cancelOrder(UUID.randomUUID()));
    }
}
