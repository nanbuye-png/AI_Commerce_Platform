package com.commerce.platform.product.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.product.dto.customer.*;
import com.commerce.platform.product.entity.Category;
import com.commerce.platform.product.entity.Product;
import com.commerce.platform.product.entity.ProductImage;
import com.commerce.platform.product.enums.ProductStatus;
import com.commerce.platform.product.repository.CategoryRepository;
import com.commerce.platform.product.repository.ProductRepository;
import com.commerce.platform.product.service.CustomerProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * C端商品浏览服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProductServiceImpl implements CustomerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final int PRODUCT_NOT_FOUND = 30001;
    private static final int PRODUCT_OFFLINE = 30005;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> listProducts(ProductSearchRequest request) {
        // 构建排序规则
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<Product> productPage;

        boolean hasKeyword = StringUtils.hasText(request.getKeyword());
        boolean hasCategory = request.getCategoryId() != null && request.getCategoryId() > 0;
        boolean hasPriceRange = request.getMinPrice() != null || request.getMaxPrice() != null;

        if (hasPriceRange) {
            productPage = productRepository.searchCustomerProductsByPrice(
                    ProductStatus.ON_SHELF,
                    hasKeyword ? request.getKeyword() : null,
                    hasCategory ? request.getCategoryId() : null,
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    pageRequest
            );
        } else if (hasKeyword && hasCategory) {
            productPage = productRepository.findByStatusAndProductNameContainingAndCategoryId(
                    ProductStatus.ON_SHELF, request.getKeyword(), request.getCategoryId(), pageRequest);
        } else if (hasKeyword) {
            productPage = productRepository.findByStatusAndProductNameContaining(
                    ProductStatus.ON_SHELF, request.getKeyword(), pageRequest);
        } else if (hasCategory) {
            productPage = productRepository.findByStatusAndCategoryId(
                    ProductStatus.ON_SHELF, request.getCategoryId(), pageRequest);
        } else {
            productPage = productRepository.findByStatus(ProductStatus.ON_SHELF, pageRequest);
        }

        return productPage.map(this::convertToCardResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ON_SHELF)
                .orElseThrow(() -> new BusinessException(PRODUCT_OFFLINE, "商品不存在或已下架"));

        return convertToDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        // 获取所有一级分类（parentId = 0）
        List<Category> rootCategories = categoryRepository.findByParentIdOrderBySortAsc(0L);
        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 递归构建分类树
     */
    private CategoryTreeResponse buildCategoryTree(Category category) {
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        response.setParentId(category.getParentId());
        response.setLevel(category.getLevel());
        response.setSort(category.getSort());

        // 递归获取子分类
        List<Category> children = categoryRepository.findByParentIdOrderBySortAsc(category.getId());
        if (children != null && !children.isEmpty()) {
            response.setChildren(children.stream()
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList()));
        } else {
            response.setChildren(Collections.emptyList());
        }

        return response;
    }

    /**
     * 构建排序
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        if (!StringUtils.hasText(sortBy)) {
            sortBy = "createdTime";
        }
        Sort.Direction direction;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, sortBy);
    }

    /**
     * 转换为列表卡片响应
     */
    private ProductCardResponse convertToCardResponse(Product product) {
        ProductCardResponse response = new ProductCardResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategoryId());
        response.setSalesCount(product.getSalesCount());
        response.setCreatedTime(product.getCreatedTime());

        // 计算价格区间
        if (product.getSkus() != null && !product.getSkus().isEmpty()) {
            BigDecimal min = product.getSkus().stream()
                    .map(sku -> sku.getPrice())
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal max = product.getSkus().stream()
                    .map(sku -> sku.getPrice())
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            response.setMinPrice(min);
            response.setMaxPrice(max);
        }

        // 取首图
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            product.getImages().stream()
                    .filter(ProductImage::getIsCover)
                    .findFirst()
                    .ifPresent(img -> response.setCoverImage(img.getUrl()));
            if (response.getCoverImage() == null) {
                response.setCoverImage(product.getImages().get(0).getUrl());
            }
        }

        return response;
    }

    /**
     * 转换为详情响应
     */
    private ProductDetailResponse convertToDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategoryId());
        response.setSalesCount(product.getSalesCount());
        response.setCreatedTime(product.getCreatedTime());

        // 图片
        if (product.getImages() != null) {
            response.setImages(product.getImages().stream().map(img -> {
                ProductDetailResponse.ProductImageVO vo = new ProductDetailResponse.ProductImageVO();
                vo.setUrl(img.getUrl());
                vo.setImageType(img.getImageType().name());
                vo.setSort(img.getSort());
                vo.setIsCover(img.getIsCover());
                return vo;
            }).collect(Collectors.toList()));
        }

        // 规格
        if (product.getSpecs() != null) {
            response.setSpecs(product.getSpecs().stream().map(spec -> {
                ProductDetailResponse.ProductSpecVO vo = new ProductDetailResponse.ProductSpecVO();
                vo.setSpecName(spec.getSpecName());
                vo.setSpecValues(spec.getSpecValues());
                vo.setSort(spec.getSort());
                return vo;
            }).collect(Collectors.toList()));
        }

        // SKU
        if (product.getSkus() != null) {
            response.setSkus(product.getSkus().stream().map(sku -> {
                ProductDetailResponse.ProductSkuVO vo = new ProductDetailResponse.ProductSkuVO();
                vo.setId(sku.getId());
                vo.setSkuCode(sku.getSkuCode());
                vo.setAttributesJson(sku.getAttributesJson());
                vo.setPrice(sku.getPrice());
                vo.setOriginalPrice(sku.getOriginalPrice());
                vo.setWeight(sku.getWeight());
                vo.setStatus(sku.getStatus());
                return vo;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}