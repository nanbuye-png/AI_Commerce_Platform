package com.commerce.platform.refund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRefundRequest {
    @NotBlank(message = "退款原因不能为空")
    private String reason;
}
