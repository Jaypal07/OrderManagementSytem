package com.jaypal.oms.order.infrastructure.adapter;

import com.jaypal.oms.catalog.api.CatalogApi;
import com.jaypal.oms.order.application.port.out.CatalogPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogAdapter implements CatalogPort {

    private final CatalogApi catalogApi;

    public CatalogAdapter(CatalogApi catalogApi) {
        this.catalogApi = catalogApi;
    }

    @Override
    public Optional<BigDecimal> getPrice(String sku) {
        return catalogApi.getProduct(sku)
                .map(p -> p.price());
    }
}
