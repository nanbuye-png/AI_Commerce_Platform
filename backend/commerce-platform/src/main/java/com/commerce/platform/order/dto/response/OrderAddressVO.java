package com.commerce.platform.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单收货地址 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddressVO {

    private Long id;

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private String postalCode;
}