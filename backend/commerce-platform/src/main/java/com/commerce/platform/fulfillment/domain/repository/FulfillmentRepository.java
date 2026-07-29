package com.commerce.platform.fulfillment.domain.repository;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * 履约单仓储接口
 * <p>
 * 定义履约单聚合的持久化操作接口，属于 Port（出站端口）。
 * 仅定义接口，不实现业务逻辑，实现由 Infrastructure 层完成。
 * </p>
 */
public interface FulfillmentRepository {

    /**
     * 保存履约单
     *
     * @param fulfillment 履约单聚合
     * @return 保存后的履约单（含生成的ID）
     */
    Fulfillment save(Fulfillment fulfillment);

    /**
     * 根据ID查询履约单
     *
     * @param id 履约单ID
     * @return 履约单 Optional
     */
    Optional<Fulfillment> findById(Long id);

    /**
     * 根据订单ID查询履约单
     *
     * @param orderId 订单ID
     * @return 履约单 Optional
     */
    Optional<Fulfillment> findByOrderId(Long orderId);

    /**
     * 判断订单是否已有履约单
     *
     * @param orderId 订单ID
     * @return true 如果已存在
     */
    boolean existsByOrderId(Long orderId);

    /**
     * 根据状态查询履约单列表
     *
     * @param status 履约状态
     * @return 履约单列表
     */
    List<Fulfillment> findByStatus(FulfillmentStatus status);
}