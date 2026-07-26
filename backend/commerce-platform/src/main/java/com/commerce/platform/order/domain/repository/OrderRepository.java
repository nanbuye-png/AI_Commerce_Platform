package com.commerce.platform.order.domain.repository;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 订单 Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ---- Customer queries ----
    Optional<Order> findByOrderNo(String orderNo);
    Optional<Order> findByBuyerIdAndOrderNo(Long buyerId, String orderNo);
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    Page<Order> findByBuyerIdAndOrderStatus(Long buyerId, OrderStatus orderStatus, Pageable pageable);
    Page<Order> findByBuyerIdAndCreatedTimeBetween(Long buyerId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Order> findByBuyerIdAndOrderStatusAndCreatedTimeBetween(Long buyerId, OrderStatus orderStatus, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // ---- Merchant queries ----
    Optional<Order> findByMerchantIdAndOrderNo(Long merchantId, String orderNo);
    Page<Order> findByMerchantId(Long merchantId, Pageable pageable);
    Page<Order> findByMerchantIdAndOrderStatus(Long merchantId, OrderStatus orderStatus, Pageable pageable);
    Page<Order> findByMerchantIdAndCreatedTimeBetween(Long merchantId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Order> findByMerchantIdAndOrderStatusAndCreatedTimeBetween(Long merchantId, OrderStatus orderStatus, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // ---- Admin queries ----
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
}