package com.jaypal.oms.catalog.api;

import java.math.BigDecimal;

public record ProductView(
        String sku,
        String name,
        BigDecimal price,
        boolean active) {
}
