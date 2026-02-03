package com.jaypal.oms.order.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String status,
        Instant createdAt,
        List<OrderItemResponse> items
) {}
