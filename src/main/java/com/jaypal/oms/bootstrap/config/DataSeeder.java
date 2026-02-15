package com.jaypal.oms.bootstrap.config;

import com.jaypal.oms.catalog.infrastructure.persistence.ProductJpaEntity;
import com.jaypal.oms.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.jaypal.oms.inventory.infrastructure.persistence.InventoryJpaEntity;
import com.jaypal.oms.inventory.infrastructure.persistence.SpringDataInventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(
            SpringDataProductRepository productRepository,
            SpringDataInventoryRepository inventoryRepository) {

        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new ProductJpaEntity("SKU-123", "Laptop", new BigDecimal("1200.00"), true),
                        new ProductJpaEntity("SKU-456", "Mouse", new BigDecimal("25.50"), true),
                        new ProductJpaEntity("SKU-789", "Monitor", new BigDecimal("300.00"), true)));
            }

            if (inventoryRepository.count() == 0) {
                inventoryRepository.saveAll(List.of(
                        new InventoryJpaEntity("SKU-123", 100, 0),
                        new InventoryJpaEntity("SKU-456", 50, 0),
                        new InventoryJpaEntity("SKU-789", 10, 0)));
            }
        };
    }
}
