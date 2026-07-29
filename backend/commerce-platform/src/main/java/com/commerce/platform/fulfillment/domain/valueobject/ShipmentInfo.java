package com.commerce.platform.fulfillment.domain.valueobject;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 物流信息值对象
 * <p>
 * 表示履约单的物流配送信息，为不可变对象（Immutable）。
 * 不提供 setter，所有字段在构造时初始化。
 * 允许后续扩展物流平台信息。
 * </p>
 */
@Getter
public final class ShipmentInfo {

    /** 物流承运商 */
    private final String carrier;

    /** 物流承运商编码 */
    private final String carrierCode;

    /** 运单号 */
    private final String trackingNumber;

    /** 收货地址 */
    private final String shippingAddress;

    /** 预计送达时间 */
    private final LocalDateTime estimatedArrival;

    /**
     * 构造物流信息
     *
     * @param carrier          物流承运商
     * @param carrierCode      物流承运商编码
     * @param trackingNumber   运单号
     * @param shippingAddress  收货地址
     * @param estimatedArrival 预计送达时间
     */
    public ShipmentInfo(
            @NotBlank String carrier,
            @NotBlank String carrierCode,
            @NotBlank String trackingNumber,
            @NotBlank String shippingAddress,
            LocalDateTime estimatedArrival) {
        this.carrier = carrier;
        this.carrierCode = carrierCode;
        this.trackingNumber = trackingNumber;
        this.shippingAddress = shippingAddress;
        this.estimatedArrival = estimatedArrival;
    }

    /**
     * 获取完整的物流追踪标识
     *
     * @return 格式：carrierCode:trackingNumber
     */
    public String getTrackingId() {
        return carrierCode + ":" + trackingNumber;
    }
}