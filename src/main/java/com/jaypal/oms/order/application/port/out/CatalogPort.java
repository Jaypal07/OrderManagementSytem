package com.jaypal.oms.order.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

public interface CatalogPort {
    /**
     * Fetches the price for a given SKU.
     * 
     * @param sku Product SKU
     * @return Price if found, empty otherwise.
     */
    Optional<BigDecimal> getPrice(String sku);
}
