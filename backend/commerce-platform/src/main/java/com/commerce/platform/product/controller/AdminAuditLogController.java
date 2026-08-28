package com.commerce.platform.product.controller;

import com.commerce.platform.common.entity.PageResult;
import com.commerce.platform.common.entity.Result;
import com.commerce.platform.product.dto.admin.AdminAuditLogResponse;
import com.commerce.platform.product.entity.ProductAuditRecord;
import com.commerce.platform.product.repository.ProductAuditRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin 审计日志查询接口。
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final ProductAuditRecordRepository auditRecordRepository;

    @GetMapping
    public Result<PageResult<AdminAuditLogResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Page<ProductAuditRecord> records = auditRecordRepository.findAll(
                PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdTime")));
        Page<AdminAuditLogResponse> responses = records.map(this::toResponse);
        return Result.success(PageResult.of(responses));
    }

    private AdminAuditLogResponse toResponse(ProductAuditRecord record) {
        AdminAuditLogResponse response = new AdminAuditLogResponse();
        response.setId(record.getId());
        response.setProductId(record.getProductId());
        response.setReviewerId(record.getReviewerId());
        response.setAction(record.getAction());
        response.setBeforeStatus(record.getBeforeStatus());
        response.setAfterStatus(record.getAfterStatus());
        response.setAuditRemark(record.getAuditRemark());
        response.setCreatedTime(record.getCreatedTime());
        return response;
    }
}
