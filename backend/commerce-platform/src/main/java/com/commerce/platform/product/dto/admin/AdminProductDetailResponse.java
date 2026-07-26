package com.commerce.platform.product.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin 商品详情响应 DTO
 */
@Data
public class AdminProductDetailResponse {

    private Long id;
    private String productCode;
    private String productName;
    private String description;
    private String brand;
    private Long categoryId;
    private Long merchantId;
    private String merchantName;
    private Long storeId;
    private String status;
    private Integer salesCount;
    private Long version;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    private List<ImageVO> images;
    private List<SpecVO> specs;
    private List<SkuVO> skus;

    @Data
    public static class ImageVO {
        private Long id;
        private String url;
        private String imageType;
        private Integer sort;
        private Boolean isCover;
    }

    @Data
    public static class SpecVO {
        private Long id;
        private String specName;
        private String specValues;
        private Integer sort;
    }

    @Data
    public static class SkuVO {
        private Long id;
        private String skuCode;
        private String attributesJson;
        private java.math.BigDecimal price;
        private java.math.BigDecimal originalPrice;
        private java.math.BigDecimal weight;
        private String status;
        private Integer salesCount;
    }
}