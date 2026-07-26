package com.commerce.platform.product.dto.customer;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * C端商品详情响应 DTO
 */
@Data
public class ProductDetailResponse {

    private Long id;
    private String productName;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private String storeName;
    private Long storeId;
    private Integer salesCount;
    private LocalDateTime createdTime;

    private List<ProductImageVO> images;
    private List<ProductSpecVO> specs;
    private List<ProductSkuVO> skus;

    @Data
    public static class ProductImageVO {
        private String url;
        private String imageType;
        private Integer sort;
        private Boolean isCover;
    }

    @Data
    public static class ProductSpecVO {
        private String specName;
        private String specValues;
        private Integer sort;
    }

    @Data
    public static class ProductSkuVO {
        private Long id;
        private String skuCode;
        private String attributesJson;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private BigDecimal weight;
        private String status;
    }
}