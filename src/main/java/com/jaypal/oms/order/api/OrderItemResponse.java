package com.jaypal.oms.order.api;

import java.math.BigDecimal;

public record OrderItemResponse(
        String sku,
        int quantity,
        BigDecimal unitPrice
) {}
