package com.commerce.platform.fulfillment.infrastructure.persistence;

import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 履约单 JPA 数据访问接口
 * <p>
 * Infrastructure 层，继承 Spring Data JPA 提供的基础 CRUD。
 * 由 FulfillmentRepositoryImpl 适配调用，非 Domain 层直接使用。
 * </p>
 */
@Repository
interface FulfillmentJpaRepository extends JpaRepository<FulfillmentEntity, Long> {

    /**
     * 根据订单ID查询履约单
     *
     * @param orderId 订单ID
     * @return 履约单 Optional
     */
    Optional<FulfillmentEntity> findByOrderId(Long orderId);

    /**
     * 判断订单ID是否存在
     *
     * @param orderId 订单ID
     * @return true 如果存在
     */
    boolean existsByOrderId(Long orderId);

    /**
     * 根据状态查询履约单列表
     *
     * @param status 履约状态
     * @return 履约单列表
     */
    List<FulfillmentEntity> findByStatus(FulfillmentStatus status);
}