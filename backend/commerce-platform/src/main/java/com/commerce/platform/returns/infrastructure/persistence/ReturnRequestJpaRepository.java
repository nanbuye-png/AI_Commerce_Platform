package com.commerce.platform.returns.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退货 JPA Repository
 */
@Repository
public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequestEntity, Long>,
        JpaSpecificationExecutor<ReturnRequestEntity> {

    List<ReturnRequestEntity> findByOrderId(Long orderId);

    List<ReturnRequestEntity> findByUserId(Long userId);
}