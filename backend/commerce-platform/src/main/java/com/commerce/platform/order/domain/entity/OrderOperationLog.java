package com.commerce.platform.order.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 订单操作日志实体
 * <p>
 * 记录订单生命周期中的操作行为，独立于 Order Entity。
 * 支持：用户取消、商家发货、管理员取消、管理员关闭、系统自动关闭等。
 * </p>
 */
@Entity
@Table(name = "order_operation_logs", indexes = {
    @Index(name = "idx_log_order_no", columnList = "order_no"),
    @Index(name = "idx_log_created_time", columnList = "created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOperationLog extends BaseEntity {

    @Column(name = "order_no", nullable = false, length = 32, updatable = false)
    private String orderNo;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_type", nullable = false, length = 20)
    private String operatorType;

    @Column(name = "operation_type", nullable = false, length = 32)
    private String operationType;

    @Column(length = 500)
    private String reason;
}