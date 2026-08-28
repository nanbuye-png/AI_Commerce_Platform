package com.commerce.platform.returns.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerReturnRequest {
    @NotBlank(message = "退货原因不能为空")
    private String reason;
}
