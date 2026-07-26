package com.commerce.platform.product.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.product.dto.admin.*;
import com.commerce.platform.product.entity.Product;
import com.commerce.platform.product.entity.ProductAuditRecord;
import com.commerce.platform.product.entity.ProductImage;
import com.commerce.platform.product.enums.ProductStatus;
import com.commerce.platform.product.mq.event.ProductApprovedEvent;
import com.commerce.platform.product.mq.event.ProductOffShelfEvent;
import com.commerce.platform.product.mq.event.ProductRejectedEvent;
import com.commerce.platform.product.repository.ProductAuditRecordRepository;
import com.commerce.platform.product.repository.ProductRepository;
import com.commerce.platform.product.service.ProductAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 商品审核与生命周期管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAuditServiceImpl implements ProductAuditService {

    private final ProductRepository productRepository;
    private final ProductAuditRecordRepository auditRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 错误码
    private static final int PRODUCT_NOT_FOUND = 30001;
    private static final int PRODUCT_ILLEGAL_STATUS = 30101;
    private static final int PRODUCT_ALREADY_AUDITED = 30102;
    private static final int PRODUCT_ALREADY_OFFLINE = 30103;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductListResponse> listPendingProducts(AdminProductQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(
                request.getPage() - 1, request.getSize(),
                Sort.by(Sort.Direction.ASC, "createdTime"));

        Page<Product> productPage = productRepository.findByStatus(
                ProductStatus.PENDING_REVIEW, pageRequest);

        return productPage.map(this::convertToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND, "商品不存在"));

        return convertToDetailResponse(product);
    }

    @Override
    @Transactional
    public void approveProduct(Long productId, Long reviewerId, ProductAuditRequest request) {
        log.info("Approving product: {} by reviewer: {}", productId, reviewerId);
        Product product = findProductOrThrow(productId);

        // 状态机校验：仅 PENDING_REVIEW → ON_SHELF
        if (product.getStatus() != ProductStatus.PENDING_REVIEW) {
            throw new BusinessException(PRODUCT_ILLEGAL_STATUS,
                    "当前状态不允许审核通过，商品状态: " + product.getStatus());
        }

        ProductStatus beforeStatus = product.getStatus();
        product.setStatus(ProductStatus.ON_SHELF);
        productRepository.save(product);

        // 记录审核日志
        saveAuditRecord(productId, reviewerId, "APPROVE", beforeStatus, ProductStatus.ON_SHELF, request.getAuditRemark());

        // 发布事件
        eventPublisher.publishEvent(new ProductApprovedEvent(productId, reviewerId, request.getAuditRemark()));
        log.info("Product approved: {}", productId);
    }

    @Override
    @Transactional
    public void rejectProduct(Long productId, Long reviewerId, ProductAuditRequest request) {
        log.info("Rejecting product: {} by reviewer: {}", productId, reviewerId);
        Product product = findProductOrThrow(productId);

        // 状态机校验：仅 PENDING_REVIEW → REJECTED
        if (product.getStatus() != ProductStatus.PENDING_REVIEW) {
            throw new BusinessException(PRODUCT_ILLEGAL_STATUS,
                    "当前状态不允许审核驳回，商品状态: " + product.getStatus());
        }

        ProductStatus beforeStatus = product.getStatus();
        product.setStatus(ProductStatus.REJECTED);
        productRepository.save(product);

        // 记录审核日志
        saveAuditRecord(productId, reviewerId, "REJECT", beforeStatus, ProductStatus.REJECTED, request.getAuditRemark());

        // 发布事件
        eventPublisher.publishEvent(new ProductRejectedEvent(productId, reviewerId, request.getAuditRemark()));
        log.info("Product rejected: {}", productId);
    }

    @Override
    @Transactional
    public void forceOffShelf(Long productId, Long reviewerId, ProductAuditRequest request) {
        log.info("Force off-shelf product: {} by reviewer: {}", productId, reviewerId);
        Product product = findProductOrThrow(productId);

        // 状态机校验：仅 ON_SHELF → OFF_SHELF
        if (product.getStatus() != ProductStatus.ON_SHELF) {
            throw new BusinessException(PRODUCT_ALREADY_OFFLINE,
                    "仅上架商品可强制下架，当前状态: " + product.getStatus());
        }

        ProductStatus beforeStatus = product.getStatus();
        product.setStatus(ProductStatus.OFF_SHELF);
        productRepository.save(product);

        // 记录审核日志
        saveAuditRecord(productId, reviewerId, "FORCE_OFF_SHELF", beforeStatus, ProductStatus.OFF_SHELF, request.getAuditRemark());

        // 发布事件
        eventPublisher.publishEvent(new ProductOffShelfEvent(productId, reviewerId, request.getAuditRemark()));
        log.info("Product force off-shelf: {}", productId);
    }

    @Override
    @Transactional
    public void restoreProduct(Long productId, Long reviewerId, ProductAuditRequest request) {
        log.info("Restoring product: {} by reviewer: {}", productId, reviewerId);
        Product product = findProductOrThrow(productId);

        // 状态机校验：仅 OFF_SHELF → ON_SHELF
        if (product.getStatus() != ProductStatus.OFF_SHELF) {
            throw new BusinessException(PRODUCT_ILLEGAL_STATUS,
                    "仅下架商品可恢复上架，当前状态: " + product.getStatus());
        }

        ProductStatus beforeStatus = product.getStatus();
        product.setStatus(ProductStatus.ON_SHELF);
        productRepository.save(product);

        // 记录审核日志
        saveAuditRecord(productId, reviewerId, "RESTORE", beforeStatus, ProductStatus.ON_SHELF, request.getAuditRemark());
        log.info("Product restored: {}", productId);
    }

    // ==================== 私有方法 ====================

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND, "商品不存在"));
    }

    private void saveAuditRecord(Long productId, Long reviewerId, String action,
                                  ProductStatus beforeStatus, ProductStatus afterStatus,
                                  String auditRemark) {
        ProductAuditRecord record = ProductAuditRecord.builder()
                .productId(productId)
                .reviewerId(reviewerId)
                .action(action)
                .beforeStatus(beforeStatus.name())
                .afterStatus(afterStatus.name())
                .auditRemark(auditRemark)
                .build();
        auditRecordRepository.save(record);
    }

    private AdminProductListResponse convertToListResponse(Product product) {
        AdminProductListResponse response = new AdminProductListResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setProductName(product.getProductName());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategoryId());
        response.setMerchantId(product.getMerchantId());
        response.setStatus(product.getStatus().name());
        response.setSalesCount(product.getSalesCount());
        response.setCreatedTime(product.getCreatedTime());
        response.setUpdatedTime(product.getUpdatedTime());

        // 首图
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

    private AdminProductDetailResponse convertToDetailResponse(Product product) {
        AdminProductDetailResponse response = new AdminProductDetailResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategoryId(product.getCategoryId());
        response.setMerchantId(product.getMerchantId());
        response.setStoreId(product.getStoreId());
        response.setStatus(product.getStatus().name());
        response.setSalesCount(product.getSalesCount());
        response.setVersion(product.getVersion());
        response.setCreatedTime(product.getCreatedTime());
        response.setUpdatedTime(product.getUpdatedTime());

        // 图片
        if (product.getImages() != null) {
            response.setImages(product.getImages().stream().map(img -> {
                AdminProductDetailResponse.ImageVO vo = new AdminProductDetailResponse.ImageVO();
                vo.setId(img.getId());
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
                AdminProductDetailResponse.SpecVO vo = new AdminProductDetailResponse.SpecVO();
                vo.setId(spec.getId());
                vo.setSpecName(spec.getSpecName());
                vo.setSpecValues(spec.getSpecValues());
                vo.setSort(spec.getSort());
                return vo;
            }).collect(Collectors.toList()));
        }

        // SKU
        if (product.getSkus() != null) {
            response.setSkus(product.getSkus().stream().map(sku -> {
                AdminProductDetailResponse.SkuVO vo = new AdminProductDetailResponse.SkuVO();
                vo.setId(sku.getId());
                vo.setSkuCode(sku.getSkuCode());
                vo.setAttributesJson(sku.getAttributesJson());
                vo.setPrice(sku.getPrice());
                vo.setOriginalPrice(sku.getOriginalPrice());
                vo.setWeight(sku.getWeight());
                vo.setStatus(sku.getStatus());
                vo.setSalesCount(sku.getSalesCount());
                return vo;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}