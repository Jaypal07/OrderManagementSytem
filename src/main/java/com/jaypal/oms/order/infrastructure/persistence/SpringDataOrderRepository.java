package com.jaypal.oms.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataOrderRepository
        extends JpaRepository<OrderJpaEntity, UUID> {
}
