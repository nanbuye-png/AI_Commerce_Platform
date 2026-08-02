package com.commerce.platform.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 收货地址创建/更新请求
 */
@Data
public class AddressRequest {

    @NotBlank(message = "收件人不能为空")
    private String receiver;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private String postalCode;

    private Boolean isDefault;
}