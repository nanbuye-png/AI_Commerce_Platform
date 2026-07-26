package com.commerce.platform.order.domain.repository;

import com.commerce.platform.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单条目 Repository
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 按订单 ID 查询所有条目
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 按订单 ID 和 SKU ID 查询
     */
    List<OrderItem> findByOrderIdAndSkuId(Long orderId, Long skuId);
}