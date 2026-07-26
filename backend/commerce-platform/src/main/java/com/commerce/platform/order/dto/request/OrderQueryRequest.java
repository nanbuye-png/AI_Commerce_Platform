package com.commerce.platform.order.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 订单查询请求
 */
@Data
public class OrderQueryRequest {

    /**
     * 订单状态筛选（ALL 或具体状态）
     */
    private String status;

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

    /**
     * 页码，默认 1
     */
    private int page = 1;

    /**
     * 每页条数，默认 20
     */
    private int pageSize = 20;
}