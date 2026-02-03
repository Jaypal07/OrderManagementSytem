package com.jaypal.oms.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataInventoryRepository
        extends JpaRepository<InventoryJpaEntity, String> {
}
