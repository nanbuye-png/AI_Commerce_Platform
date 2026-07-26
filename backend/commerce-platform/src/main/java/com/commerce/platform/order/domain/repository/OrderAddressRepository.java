package com.commerce.platform.order.domain.repository;

import com.commerce.platform.order.domain.entity.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 订单收货地址 Repository
 */
@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {

    /**
     * 按订单 ID 查询收货地址
     */
    Optional<OrderAddress> findByOrderId(Long orderId);
}