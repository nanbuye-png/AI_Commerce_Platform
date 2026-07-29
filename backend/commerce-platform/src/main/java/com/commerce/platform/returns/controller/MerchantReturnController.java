package com.commerce.platform.returns.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import com.commerce.platform.returns.domain.valueobject.ReturnStatus;
import com.commerce.platform.returns.infrastructure.persistence.ReturnRequestEntity;
import com.commerce.platform.returns.infrastructure.persistence.ReturnRequestJpaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Merchant 退货管理 Controller
 * <p>
 * 只允许 MERCHANT 角色访问。
 * 商家可以查看自己商品的退货申请，并批准或拒绝。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantReturnController {

    private final ReturnRequestJpaRepository returnJpaRepository;
    private final ReturnRepository returnRepository;

    /**
     * 退货列表查询（分页）
     */
    @GetMapping
    public Result<Page<ReturnRequest>> listReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 退货列表查询 - merchantId={}, page={}, size={}, status={}", merchantId, page, pageSize, status);

        Specification<ReturnRequestEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                try {
                    ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), returnStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的退货状态参数: {}", status);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReturnRequestEntity> entityPage = returnJpaRepository.findAll(spec, pageRequest);
        Page<ReturnRequest> returnPage = entityPage.map(this::toDomain);
        log.info("Merchant 退货列表查询完成 - total={}", returnPage.getTotalElements());
        return Result.success(returnPage);
    }

    /**
     * 退货详情
     */
    @GetMapping("/{id}")
    public Result<ReturnRequest> getReturnDetail(@PathVariable Long id) {
        log.info("Merchant 退货详情 - id={}", id);
        ReturnRequest returnRequest = returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退货不存在: " + id));
        return Result.success(returnRequest);
    }

    /**
     * 商家批准退货
     * REQUESTED → APPROVED
     */
    @PostMapping("/{id}/approve")
    public Result<ReturnRequest> approveReturn(@PathVariable Long id, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 批准退货 - id={}, merchantId={}", id, merchantId);

        ReturnRequest returnRequest = returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退货不存在: " + id));

        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            return Result.error("当前退货状态不允许批准: " + returnRequest.getStatus());
        }

        returnRequest.approve();
        ReturnRequest saved = returnRepository.save(returnRequest);
        log.info("Merchant 批准退货成功 - id={}, status={}", id, saved.getStatus());
        return Result.success(saved);
    }

    /**
     * 商家拒绝退货
     * REQUESTED → REJECTED
     */
    @PostMapping("/{id}/reject")
    public Result<ReturnRequest> rejectReturn(@PathVariable Long id, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 拒绝退货 - id={}, merchantId={}", id, merchantId);

        ReturnRequest returnRequest = returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退货不存在: " + id));

        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            return Result.error("当前退货状态不允许拒绝: " + returnRequest.getStatus());
        }

        returnRequest.reject();
        ReturnRequest saved = returnRepository.save(returnRequest);
        log.info("Merchant 拒绝退货成功 - id={}, status={}", id, saved.getStatus());
        return Result.success(saved);
    }

    private ReturnRequest toDomain(ReturnRequestEntity entity) {
        return ReturnRequest.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getRefundId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getApprovedAt(),
                entity.getCompletedAt()
        );
    }

    private Long getMerchantId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("未认证的请求");
        }
        return (Long) authentication.getPrincipal();
    }
}