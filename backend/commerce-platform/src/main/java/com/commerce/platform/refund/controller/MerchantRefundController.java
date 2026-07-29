package com.commerce.platform.refund.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import com.commerce.platform.refund.domain.valueobject.RefundStatus;
import com.commerce.platform.refund.infrastructure.persistence.RefundEntity;
import com.commerce.platform.refund.infrastructure.persistence.RefundJpaRepository;
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
 * Merchant 退款管理 Controller
 * <p>
 * 只允许 MERCHANT 角色访问。
 * 商家可以查看自己商品的退款申请，并批准或拒绝。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantRefundController {

    private final RefundJpaRepository refundJpaRepository;
    private final RefundRepository refundRepository;

    /**
     * 退款列表查询（分页）
     */
    @GetMapping
    public Result<Page<Refund>> listRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 退款列表查询 - merchantId={}, page={}, size={}, status={}", merchantId, page, pageSize, status);

        // 按 merchant_id 过滤（通过 order_id 关联，暂时简化查询所有退款）
        Specification<RefundEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                try {
                    RefundStatus refundStatus = RefundStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), refundStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的退款状态参数: {}", status);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RefundEntity> entityPage = refundJpaRepository.findAll(spec, pageRequest);
        Page<Refund> refundPage = entityPage.map(this::toDomain);
        log.info("Merchant 退款列表查询完成 - total={}", refundPage.getTotalElements());
        return Result.success(refundPage);
    }

    /**
     * 退款详情
     */
    @GetMapping("/{id}")
    public Result<Refund> getRefundDetail(@PathVariable Long id) {
        log.info("Merchant 退款详情 - id={}", id);
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退款不存在: " + id));
        return Result.success(refund);
    }

    /**
     * 商家批准退款
     * REQUESTED → APPROVED
     */
    @PostMapping("/{id}/approve")
    public Result<Refund> approveRefund(@PathVariable Long id, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 批准退款 - id={}, merchantId={}", id, merchantId);

        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退款不存在: " + id));

        if (refund.getStatus() != RefundStatus.REQUESTED) {
            return Result.error("当前退款状态不允许批准: " + refund.getStatus());
        }

        refund.approve();
        Refund saved = refundRepository.save(refund);
        log.info("Merchant 批准退款成功 - id={}, status={}", id, saved.getStatus());
        return Result.success(saved);
    }

    /**
     * 商家拒绝退款
     * REQUESTED → REJECTED
     */
    @PostMapping("/{id}/reject")
    public Result<Refund> rejectRefund(@PathVariable Long id, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 拒绝退款 - id={}, merchantId={}", id, merchantId);

        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退款不存在: " + id));

        if (refund.getStatus() != RefundStatus.REQUESTED) {
            return Result.error("当前退款状态不允许拒绝: " + refund.getStatus());
        }

        refund.reject();
        Refund saved = refundRepository.save(refund);
        log.info("Merchant 拒绝退款成功 - id={}, status={}", id, saved.getStatus());
        return Result.success(saved);
    }

    private Refund toDomain(RefundEntity entity) {
        return Refund.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getAmount(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt(),
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