package com.commerce.platform.order.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Admin 订单查询请求
 */
@Data
public class AdminOrderQueryRequest {

    /**
     * 页码，默认 1
     */
    private int page = 1;

    /**
     * 每页条数，默认 20
     */
    private int pageSize = 20;

    /**
     * 订单号（精确匹配）
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 支付状态
     */
    private String paymentStatus;

    /**
     * 发货状态
     */
    private String shippingStatus;

    /**
     * 创建时间起始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 创建时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}