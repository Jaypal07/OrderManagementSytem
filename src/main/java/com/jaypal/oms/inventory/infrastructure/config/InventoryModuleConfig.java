package com.jaypal.oms.inventory.infrastructure.config;

import com.jaypal.oms.inventory.application.port.out.InventoryRepositoryPort;
import com.jaypal.oms.inventory.application.usecase.ReleaseStockUseCase;
import com.jaypal.oms.inventory.application.usecase.ReserveStockUseCase;
import com.jaypal.oms.inventory.infrastructure.persistence.InventoryRepositoryAdapter;
import com.jaypal.oms.inventory.infrastructure.persistence.SpringDataInventoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory Module Configuration
 *
 * Configures the Inventory module with:
 * - Spring Retry for automatic retry on OptimisticLockException
 * - Repository adapter for persistence
 * - Use case beans for stock operations
 */
@Configuration
@EnableRetry // Enable @Retryable processing for use cases
public class InventoryModuleConfig {

    @Bean
    InventoryRepositoryPort inventoryRepositoryPort(
            SpringDataInventoryRepository repository) {
        return new InventoryRepositoryAdapter(repository);
    }

    @Bean
    @Transactional
    ReserveStockUseCase reserveStockUseCase(
            InventoryRepositoryPort inventoryRepositoryPort) {
        return new ReserveStockUseCase(inventoryRepositoryPort);
    }

    @Bean
    @Transactional
    ReleaseStockUseCase releaseStockUseCase(
            InventoryRepositoryPort inventoryRepositoryPort) {
        return new ReleaseStockUseCase(inventoryRepositoryPort);
    }
}


