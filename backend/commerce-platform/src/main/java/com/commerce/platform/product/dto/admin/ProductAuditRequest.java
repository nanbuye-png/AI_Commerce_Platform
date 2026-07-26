package com.commerce.platform.product.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Admin 审核商品请求 DTO
 */
@Data
public class ProductAuditRequest {

    @NotBlank(message = "审核备注不能为空")
    @Size(max = 500, message = "审核备注最长500个字符")
    private String auditRemark;
}