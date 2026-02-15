package com.jaypal.oms.bootstrap.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics Configuration for OMS
 *
 * Defines business metrics for orders, stock, and failures.
 * Metrics are exposed via Spring Boot Actuator / Prometheus.
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Exposes OmsMetrics as a Spring Bean for injection into services
     */
    @Bean
    public OmsMetrics omsMetrics(MeterRegistry meterRegistry) {
        return new OmsMetrics(meterRegistry);
    }

    /**
     * OmsMetrics provides methods to record business-level metrics
     * such as orders created, stock reserved, and failures.
     */
    public static class OmsMetrics {

        private final MeterRegistry meterRegistry;

        public OmsMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        // ===================== Orders =====================

        /**
         * Record a successfully created order.
         * Optional tag: channel
         */
        public void recordOrderCreated(String channel) {
            Counter.builder("orders.created")
                    .description("Total number of orders created successfully")
                    .tags(channel != null ? Tags.of("channel", channel) : Tags.empty())
                    .register(meterRegistry)
                    .increment();
        }

        /**
         * Record an order cancellation.
         * Optional tag: reason
         */
        public void recordOrderCancelled(String reason) {
            Counter.builder("orders.cancelled")
                    .description("Total number of orders cancelled")
                    .tags(reason != null ? Tags.of("reason", reason) : Tags.empty())
                    .register(meterRegistry)
                    .increment();
        }

        // ===================== Stock =====================

        /**
         * Record successful stock reservation.
         * Optional tag: SKU
         */
        public void recordStockReserved(int quantity, String sku) {
            Counter.builder("stock.reserved")
                    .description("Total units of stock successfully reserved")
                    .tags(sku != null ? Tags.of("sku", sku) : Tags.empty())
                    .register(meterRegistry)
                    .increment(quantity);
        }

        /**
         * Record stock reservation failure.
         * Optional tags: SKU, reason
         */
        public void recordStockReservationFailed(String sku, String reason) {
            Counter.builder("stock.reservation.failed")
                    .description("Total number of stock reservation failures")
                    .tags(
                            sku != null ? Tags.of("sku", sku) : Tags.empty()
                    ).tags(
                            reason != null ? Tags.of("reason", reason) : Tags.empty()
                    )
                    .register(meterRegistry)
                    .increment();
        }

        // ===================== Utility =====================

        public MeterRegistry getMeterRegistry() {
            return meterRegistry;
        }
    }
}
