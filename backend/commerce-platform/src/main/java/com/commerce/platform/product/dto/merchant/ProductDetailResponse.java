package com.commerce.platform.product.dto.merchant;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家端商品详情响应 DTO
 */
@Data
public class ProductDetailResponse {

    private Long id;
    private String productCode;
    private String productName;
    private String description;
    private String brand;
    private Long categoryId;
    private String status;
    private Integer salesCount;
    private Long version;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    private List<ProductImageResponse> images;
    private List<ProductSpecResponse> specs;
    private List<ProductSkuResponse> skus;

    @Data
    public static class ProductImageResponse {
        private Long id;
        private String url;
        private String imageType;
        private Integer sort;
        private Boolean isCover;
    }

    @Data
    public static class ProductSpecResponse {
        private Long id;
        private String specName;
        private String specValues;
        private Integer sort;
    }

    @Data
    public static class ProductSkuResponse {
        private Long id;
        private String skuCode;
        private String attributesJson;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private BigDecimal weight;
        private String status;
        private Integer salesCount;
        private Integer stock;
    }
}