package com.commerce.platform.product.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Admin 审计日志响应 DTO。
 * 当前日志来源为商品审核与上下架操作记录。
 */
@Data
public class AdminAuditLogResponse {

    private Long id;
    private Long productId;
    private Long reviewerId;
    private String action;
    private String beforeStatus;
    private String afterStatus;
    private String auditRemark;
    private LocalDateTime createdTime;
}
