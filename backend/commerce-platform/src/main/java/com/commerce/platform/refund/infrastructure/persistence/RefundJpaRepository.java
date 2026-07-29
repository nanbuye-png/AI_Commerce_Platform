package com.commerce.platform.refund.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 退款 JPA Repository
 */
@Repository
public interface RefundJpaRepository extends JpaRepository<RefundEntity, Long>,
        JpaSpecificationExecutor<RefundEntity> {
}