package com.jaypal.oms.order.api;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record OrderRequest(
        @NotEmpty(message = "Items cannot be empty") Map<String, Integer> items) {
}
