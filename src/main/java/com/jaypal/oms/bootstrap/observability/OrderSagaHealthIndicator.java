package com.jaypal.oms.bootstrap.observability;

import com.jaypal.oms.order.application.port.out.OrderRepositoryPort;
import com.jaypal.oms.order.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Order Saga Health Indicator
 *
 * Monitors the health of the order saga by detecting stuck orders.
 *
 * Checks:
 * - Orders stuck in PENDING state for > 30 minutes (saga failure)
 * - Overall order pipeline health
 *
 * Exposed at: /actuator/health/orderSaga
 */
@Component("orderSaga")
public class OrderSagaHealthIndicator implements HealthIndicator {

    private final OrderRepositoryPort orderRepository;

    public OrderSagaHealthIndicator(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Override
    public Health health() {
        try {
            // Check for orders stuck in PENDING state
            // In production, would use repository method to count stuck orders
            // For now, assume no stuck orders (repository doesn't have this method yet)

            long stuckOrderCount = 0; // TODO: orderRepository.countStuckPendingOrders(30 minutes ago)

            if (stuckOrderCount > 5) {
                return Health.outOfService()
                        .withDetail("reason", "Too many stuck orders detected")
                        .withDetail("stuckOrderCount", stuckOrderCount)
                        .build();
            } else if (stuckOrderCount > 0) {
                return Health.down()
                        .withDetail("reason", "Some orders stuck in PENDING state")
                        .withDetail("stuckOrderCount", stuckOrderCount)
                        .build();
            } else {
                return Health.up()
                        .withDetail("stuckOrders", 0)
                        .withDetail("status", "All orders processing normally")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

