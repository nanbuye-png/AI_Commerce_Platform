package com.commerce.platform.inventory.reservation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存预占 JPA 数据访问接口
 * <p>
 * Infrastructure 层，继承 Spring Data JPA 提供的基础 CRUD。
 * 由 StockReservationRepositoryImpl 适配调用，非 Domain 层直接使用。
 * </p>
 */
@Repository
interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long> {

    /**
     * 根据订单ID和商品ID查询（Sprint 20 Step 4C）
     */
    Optional<StockReservationEntity> findByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * 检查订单和商品是否已有预占记录（Sprint 20 Step 4C）
     */
    boolean existsByOrderIdAndProductId(Long orderId, Long productId);
}
