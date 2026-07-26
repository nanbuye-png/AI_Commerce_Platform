package com.commerce.platform.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long buyerId;

    private String buyerName;

    private Long merchantId;

    private Long storeId;

    private String storeName;

    private String orderStatus;

    private String paymentStatus;

    private String shippingStatus;

    private BigDecimal totalAmount;

    private BigDecimal productAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private String buyerRemark;

    private String merchantRemark;

    private List<OrderItemVO> items;

    private OrderAddressVO address;

    private LocalDateTime paymentTime;

    private LocalDateTime shippingTime;

    private LocalDateTime receivedTime;

    private LocalDateTime completedTime;

    private LocalDateTime cancelledTime;

    private LocalDateTime createdTime;

    /**
     * 是否可支付
     */
    private Boolean canPay;

    /**
     * 是否可取消
     */
    private Boolean canCancel;

    /**
     * 是否可确认收货
     */
    private Boolean canConfirm;

    /**
     * 是否可退款（预留）
     */
    private Boolean canRefund;

    /**
     * 显示状态
     */
    private String displayStatus;
}
