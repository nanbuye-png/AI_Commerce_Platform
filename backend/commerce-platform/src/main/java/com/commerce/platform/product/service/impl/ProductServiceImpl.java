package com.commerce.platform.product.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.product.dto.merchant.*;
import com.commerce.platform.product.entity.Product;
import com.commerce.platform.product.entity.ProductImage;
import com.commerce.platform.product.entity.ProductSku;
import com.commerce.platform.product.entity.ProductSpec;
import com.commerce.platform.product.enums.ImageType;
import com.commerce.platform.product.enums.ProductStatus;
import com.commerce.platform.product.repository.ProductRepository;
import com.commerce.platform.product.service.ProductCodeGenerator;
import com.commerce.platform.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCodeGenerator productCodeGenerator;

    // 商品域错误码起始值
    private static final int PRODUCT_NOT_FOUND = 30001;
    private static final int PRODUCT_UNAUTHORIZED = 30002;
    private static final int PRODUCT_CODE_DUPLICATE = 30003;
    private static final int PRODUCT_INVALID_STATUS = 30004;

    @Override
    @Transactional
    public Long createProduct(Long merchantId, CreateProductRequest request) {
        log.info("Creating product for merchant: {}", merchantId);

        // 检查SKU编码是否重复
        long distinctSkuCodes = request.getSkus().stream()
                .map(ProductSkuRequest::getSkuCode)
                .distinct()
                .count();
        if (distinctSkuCodes != request.getSkus().size()) {
            throw new BusinessException(PRODUCT_INVALID_STATUS, "SKU编码不能重复");
        }

        // 检查productCode唯一性
        String productCode = productCodeGenerator.generateProductCode();
        if (productRepository.findByProductCode(productCode).isPresent()) {
            throw new BusinessException(PRODUCT_CODE_DUPLICATE, "商品编码重复，请重试");
        }

        // 设置默认值
        long storeId = merchantId; // TODO: 从商家信息获取实际storeId

        // 构建 Product
        Product product = Product.builder()
                .productCode(productCode)
                .merchantId(merchantId)
                .storeId(storeId)
                .categoryId(request.getCategoryId())
                .productName(request.getProductName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .status(ProductStatus.DRAFT)
                .salesCount(0)
                .deleted(false)
                .build();

        // 构建 ProductImage 列表
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImageRequest imgReq = request.getImages().get(i);
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageType(parseImageType(imgReq.getImageType()))
                        .url(imgReq.getUrl())
                        .sort(imgReq.getSort() != null ? imgReq.getSort() : i)
                        .isCover(imgReq.getIsCover() != null ? imgReq.getIsCover() : (i == 0))
                        .deleted(false)
                        .build();
                images.add(image);
            }
            product.setImages(images);
        }

        // 构建 ProductSpec 列表
        if (request.getSpecs() != null && !request.getSpecs().isEmpty()) {
            List<ProductSpec> specs = new ArrayList<>();
            for (int i = 0; i < request.getSpecs().size(); i++) {
                ProductSpecRequest specReq = request.getSpecs().get(i);
                ProductSpec spec = ProductSpec.builder()
                        .product(product)
                        .specName(specReq.getSpecName())
                        .specValues(specReq.getSpecValues())
                        .sort(specReq.getSort() != null ? specReq.getSort() : i)
                        .deleted(false)
                        .build();
                specs.add(spec);
            }
            product.setSpecs(specs);
        }

        // 构建 ProductSku 列表
        List<ProductSku> skus = new ArrayList<>();
        for (ProductSkuRequest skuReq : request.getSkus()) {
            ProductSku sku = ProductSku.builder()
                    .product(product)
                    .skuCode(skuReq.getSkuCode())
                    .attributesJson(skuReq.getAttributesJson())
                    .price(skuReq.getPrice())
                    .originalPrice(skuReq.getOriginalPrice())
                    .weight(skuReq.getWeight() != null ? skuReq.getWeight() : java.math.BigDecimal.ZERO)
                    .status("ACTIVE")
                    .salesCount(0)
                    .deleted(false)
                    .build();
            skus.add(sku);
        }
        product.setSkus(skus);

        // Cascade 保存所有实体
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully, id: {}, code: {}", savedProduct.getId(), savedProduct.getProductCode());
        return savedProduct.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Long merchantId, Long productId, UpdateProductRequest request) {
        log.info("Updating product: {} for merchant: {}", productId, merchantId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND, "商品不存在"));

        // 校验商家权限
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(PRODUCT_UNAUTHORIZED, "无权操作此商品");
        }

        // 更新基本信息
        if (StringUtils.hasText(request.getProductName())) {
            product.setProductName(request.getProductName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getBrand())) {
            product.setBrand(request.getBrand());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }

        // 全量替换图片
        if (request.getImages() != null) {
            product.getImages().clear();
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImageRequest imgReq = request.getImages().get(i);
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageType(parseImageType(imgReq.getImageType()))
                        .url(imgReq.getUrl())
                        .sort(imgReq.getSort() != null ? imgReq.getSort() : i)
                        .isCover(imgReq.getIsCover() != null ? imgReq.getIsCover() : (i == 0))
                        .deleted(false)
                        .build();
                images.add(image);
            }
            product.getImages().addAll(images);
        }

        // 全量替换规格
        if (request.getSpecs() != null) {
            product.getSpecs().clear();
            List<ProductSpec> specs = new ArrayList<>();
            for (int i = 0; i < request.getSpecs().size(); i++) {
                ProductSpecRequest specReq = request.getSpecs().get(i);
                ProductSpec spec = ProductSpec.builder()
                        .product(product)
                        .specName(specReq.getSpecName())
                        .specValues(specReq.getSpecValues())
                        .sort(specReq.getSort() != null ? specReq.getSort() : i)
                        .deleted(false)
                        .build();
                specs.add(spec);
            }
            product.getSpecs().addAll(specs);
        }

        // 全量替换SKU
        if (request.getSkus() != null) {
            // 检查SKU编码重复
            long distinctSkuCodes = request.getSkus().stream()
                    .map(ProductSkuRequest::getSkuCode)
                    .distinct()
                    .count();
            if (distinctSkuCodes != request.getSkus().size()) {
                throw new BusinessException(PRODUCT_INVALID_STATUS, "SKU编码不能重复");
            }

            product.getSkus().clear();
            List<ProductSku> skus = new ArrayList<>();
            for (ProductSkuRequest skuReq : request.getSkus()) {
                ProductSku sku = ProductSku.builder()
                        .product(product)
                        .skuCode(skuReq.getSkuCode())
                        .attributesJson(skuReq.getAttributesJson())
                        .price(skuReq.getPrice())
                        .originalPrice(skuReq.getOriginalPrice())
                        .weight(skuReq.getWeight() != null ? skuReq.getWeight() : java.math.BigDecimal.ZERO)
                        .status("ACTIVE")
                        .salesCount(0)
                        .deleted(false)
                        .build();
                skus.add(sku);
            }
            product.getSkus().addAll(skus);
        }

        productRepository.save(product);
        log.info("Product updated successfully, id: {}", productId);
    }

    @Override
    @Transactional
    public void deleteProduct(Long merchantId, Long productId) {
        log.info("Deleting product: {} for merchant: {}", productId, merchantId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND, "商品不存在"));

        // 校验商家权限
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(PRODUCT_UNAUTHORIZED, "无权操作此商品");
        }

        // 软删除
        product.setDeleted(true);
        productRepository.save(product);
        log.info("Product soft deleted, id: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long merchantId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND, "商品不存在"));

        // 校验商家权限
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(PRODUCT_UNAUTHORIZED, "无权查看此商品");
        }

        return convertToDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> listMyProducts(Long merchantId, ProductQueryRequest query) {
        PageRequest pageRequest = PageRequest.of(
                query.getPage() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );

        Page<Product> productPage;

        if (StringUtils.hasText(query.getStatus())) {
            ProductStatus status = ProductStatus.valueOf(query.getStatus());
            productPage = productRepository.findByMerchantIdAndStatus(merchantId, status, pageRequest);
        } else {
            productPage = productRepository.findByMerchantId(merchantId, pageRequest);
        }

        return productPage.map(this::convertToListResponse);
    }

    // ==================== 私有方法 ====================

    /**
     * 解析图片类型字符串
     */
    private ImageType parseImageType(String type) {
        try {
            return ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImageType.MAIN;
        }
    }

    /**
     * 将Product实体转换为详情响应
     */
    private ProductDetailResponse convertToDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategoryId());
        response.setStatus(product.getStatus().name());
        response.setSalesCount(product.getSalesCount());
        response.setVersion(product.getVersion());
        response.setCreatedTime(product.getCreatedTime());
        response.setUpdatedTime(product.getUpdatedTime());

        // 图片
        if (product.getImages() != null) {
            response.setImages(product.getImages().stream().map(img -> {
                ProductDetailResponse.ProductImageResponse imgResp = new ProductDetailResponse.ProductImageResponse();
                imgResp.setId(img.getId());
                imgResp.setUrl(img.getUrl());
                imgResp.setImageType(img.getImageType().name());
                imgResp.setSort(img.getSort());
                imgResp.setIsCover(img.getIsCover());
                return imgResp;
            }).collect(Collectors.toList()));
        }

        // 规格
        if (product.getSpecs() != null) {
            response.setSpecs(product.getSpecs().stream().map(spec -> {
                ProductDetailResponse.ProductSpecResponse specResp = new ProductDetailResponse.ProductSpecResponse();
                specResp.setId(spec.getId());
                specResp.setSpecName(spec.getSpecName());
                specResp.setSpecValues(spec.getSpecValues());
                specResp.setSort(spec.getSort());
                return specResp;
            }).collect(Collectors.toList()));
        }

        // SKU
        if (product.getSkus() != null) {
            response.setSkus(product.getSkus().stream().map(sku -> {
                ProductDetailResponse.ProductSkuResponse skuResp = new ProductDetailResponse.ProductSkuResponse();
                skuResp.setId(sku.getId());
                skuResp.setSkuCode(sku.getSkuCode());
                skuResp.setAttributesJson(sku.getAttributesJson());
                skuResp.setPrice(sku.getPrice());
                skuResp.setOriginalPrice(sku.getOriginalPrice());
                skuResp.setWeight(sku.getWeight());
                skuResp.setStatus(sku.getStatus());
                skuResp.setSalesCount(sku.getSalesCount());
                return skuResp;
            }).collect(Collectors.toList()));
        }

        return response;
    }

    /**
     * 将Product实体转换为列表响应
     */
    private ProductListResponse convertToListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setProductName(product.getProductName());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategoryId());
        response.setStatus(product.getStatus().name());
        response.setSalesCount(product.getSalesCount());
        response.setCreatedTime(product.getCreatedTime());
        response.setUpdatedTime(product.getUpdatedTime());

        // 获取首图
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
}