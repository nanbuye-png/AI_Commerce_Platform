package com.commerce.platform.returns.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnJpaRepository extends JpaRepository<ReturnEntity, Long> {
    List<ReturnEntity> findByOrderId(Long orderId);
    List<ReturnEntity> findByUserId(Long userId);
}