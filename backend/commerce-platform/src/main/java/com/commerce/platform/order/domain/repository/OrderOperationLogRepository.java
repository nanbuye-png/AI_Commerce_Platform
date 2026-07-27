package com.commerce.platform.order.domain.repository;

import com.commerce.platform.order.domain.entity.OrderOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单操作日志 Repository
 */
@Repository
public interface OrderOperationLogRepository extends JpaRepository<OrderOperationLog, Long> {

    List<OrderOperationLog> findByOrderNoOrderByCreatedTimeDesc(String orderNo);
}