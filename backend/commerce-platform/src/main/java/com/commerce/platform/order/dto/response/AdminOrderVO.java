package com.commerce.platform.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin 订单 VO
 * 包含订单信息、Customer 信息、Merchant 信息、金额信息、状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderVO {

    // ---- 订单基本信息 ----
    private Long id;
    private String orderNo;

    // ---- Customer 信息 ----
    private Long buyerId;
    private String buyerName;

    // ---- Merchant 信息 ----
    private Long merchantId;
    private String merchantName;
    private Long storeId;
    private String storeName;

    // ---- 金额信息 ----
    private BigDecimal totalAmount;
    private BigDecimal productAmount;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;

    // ---- 状态信息 ----
    private String orderStatus;
    private String paymentStatus;
    private String shippingStatus;
    private String displayStatus;

    // ---- 备注 ----
    private String buyerRemark;
    private String merchantRemark;

    // ---- 时间节点 ----
    private LocalDateTime paymentTime;
    private LocalDateTime shippingTime;
    private LocalDateTime completedTime;
    private LocalDateTime cancelledTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // ---- 关联信息 ----
    private List<OrderItemVO> items;
    private OrderAddressVO address;
}