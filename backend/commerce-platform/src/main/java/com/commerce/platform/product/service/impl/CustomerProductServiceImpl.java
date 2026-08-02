package com.commerce.platform.product.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    private final InventoryStockRepository inventoryStockRepository;

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

        // 如果选择了分类，收集该分类及其所有子分类的 ID，使一级分类也能匹配挂在其子分类下的商品
        Set<Long> categoryIds = hasCategory
                ? collectCategoryIds(request.getCategoryId())
                : null;
        // keyword 使用空字符串而非 null，避免 PostgreSQL 无法推断参数类型（lower(bytea) 错误）
        String keyword = hasKeyword ? request.getKeyword() : "";

        if (hasPriceRange) {
            if (hasCategory) {
                productPage = productRepository.searchCustomerProductsByPriceAndCategoryIds(
                        ProductStatus.ON_SHELF,
                        keyword,
                        categoryIds,
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        pageRequest
                );
            } else {
                // 无分类 ID 时，keyword 同时匹配商品名与分类名（如"服装"、"电脑"），
                // 使品类词 + 价格区间也能正确命中商品
                productPage = productRepository.searchCustomerProductsByKeywordOrCategoryAndPrice(
                        ProductStatus.ON_SHELF,
                        keyword,
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        pageRequest
                );
            }
        } else if (hasCategory) {
            if (hasKeyword) {
                productPage = productRepository.searchCustomerProductsByKeywordAndCategoryIds(
                        ProductStatus.ON_SHELF,
                        keyword,
                        categoryIds,
                        pageRequest);
            } else {
                productPage = productRepository.findByStatusAndCategoryIds(
                        ProductStatus.ON_SHELF,
                        categoryIds,
                        pageRequest);
            }
        } else if (hasKeyword) {
            // 同时匹配商品名与分类名，使"服装"、"电脑"等品类词也能命中商品
            productPage = productRepository.findByStatusAndKeywordOrCategoryName(
                    ProductStatus.ON_SHELF, keyword, pageRequest);
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
     * 递归收集指定分类及其所有子分类的 ID 集合
     */
    private Set<Long> collectCategoryIds(Long categoryId) {
        Set<Long> ids = new LinkedHashSet<>();
        collectCategoryIdsRecursive(categoryId, ids);
        return ids;
    }

    private void collectCategoryIdsRecursive(Long categoryId, Set<Long> ids) {
        if (categoryId == null || !ids.add(categoryId)) {
            return;
        }
        List<Category> children = categoryRepository.findByParentIdOrderBySortAsc(categoryId);
        if (children != null) {
            for (Category child : children) {
                collectCategoryIdsRecursive(child.getId(), ids);
            }
        }
    }

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
                vo.setStock(inventoryStockRepository.findBySkuId(sku.getId())
                        .map(InventoryStock::getAvailableQuantity)
                        .orElse(0));
                return vo;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}