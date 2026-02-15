package com.jaypal.oms.bootstrap.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Order & Saga Metrics
 *
 * Exposes custom business metrics for monitoring order pipeline performance.
 *
 * Metrics:
 * - orders.placed: Total orders placed
 * - orders.confirmed: Total orders confirmed (stock reserved)
 * - orders.failed: Total orders failed (stock unavailable)
 * - orders.cancelled: Total orders cancelled
 * - order.placement.duration: Time from order placement to confirmation
 * - saga.stock.reserve.latency: Time for stock reservation
 */
@Component
public class OrderMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter ordersPlacedCounter;
    private final Counter ordersConfirmedCounter;
    private final Counter ordersFailedCounter;
    private final Counter ordersCancelledCounter;
    private final Timer orderPlacementTimer;
    private final Timer stockReservationTimer;

    /**
     * Default constructor for Spring bean instantiation.
     * Initializes with a null MeterRegistry (will be set via setter injection if needed).
     */
    public OrderMetrics() {
        this.meterRegistry = null;
        this.ordersPlacedCounter = null;
        this.ordersConfirmedCounter = null;
        this.ordersFailedCounter = null;
        this.ordersCancelledCounter = null;
        this.orderPlacementTimer = null;
        this.stockReservationTimer = null;
    }

    /**
     * Constructor with MeterRegistry injection.
     * Initializes all metrics (counters and timers).
     *
     * @param meterRegistry the Micrometer MeterRegistry
     */
    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.ordersPlacedCounter = Counter.builder("orders.placed")
                .description("Total orders placed")
                .register(meterRegistry);

        this.ordersConfirmedCounter = Counter.builder("orders.confirmed")
                .description("Total orders confirmed (stock reserved)")
                .register(meterRegistry);

        this.ordersFailedCounter = Counter.builder("orders.failed")
                .description("Total orders failed (stock unavailable)")
                .register(meterRegistry);

        this.ordersCancelledCounter = Counter.builder("orders.cancelled")
                .description("Total orders cancelled")
                .register(meterRegistry);

        // Initialize timers
        this.orderPlacementTimer = Timer.builder("order.placement.duration")
                .description("Time from order placement to confirmation")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.stockReservationTimer = Timer.builder("saga.stock.reserve.latency")
                .description("Time for stock reservation (saga step)")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Record order placement
     */
    public void recordOrderPlaced() {
        ordersPlacedCounter.increment();
    }

    /**
     * Record order confirmation (stock reserved)
     */
    public void recordOrderConfirmed(long durationMs) {
        ordersConfirmedCounter.increment();
        orderPlacementTimer.record(java.time.Duration.ofMillis(durationMs));
    }

    /**
     * Record order failure (stock unavailable)
     */
    public void recordOrderFailed() {
        ordersFailedCounter.increment();
    }

    /**
     * Record order cancellation
     */
    public void recordOrderCancelled() {
        ordersCancelledCounter.increment();
    }

    /**
     * Record stock reservation latency
     */
    public void recordStockReservationLatency(long durationMs) {
        stockReservationTimer.record(java.time.Duration.ofMillis(durationMs));
    }

    /**
     * Get all metric names (for documentation)
     */
    public static String getMetricNames() {
        return """
                Available Metrics:
                - orders.placed (counter): Total orders placed
                - orders.confirmed (counter): Total orders confirmed
                - orders.failed (counter): Total orders failed
                - orders.cancelled (counter): Total orders cancelled
                - order.placement.duration (timer): Placement to confirmation time
                - saga.stock.reserve.latency (timer): Stock reservation time
                
                Access via: GET /actuator/metrics/{metric-name}
                List all: GET /actuator/metrics
                """;
    }
}

