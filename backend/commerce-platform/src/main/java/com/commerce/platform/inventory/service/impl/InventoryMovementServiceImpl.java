package com.commerce.platform.inventory.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.domain.entity.InventoryMovement;
import com.commerce.platform.inventory.domain.repository.InventoryMovementRepository;
import com.commerce.platform.inventory.dto.movement.InventoryMovementDetailResponse;
import com.commerce.platform.inventory.dto.movement.InventoryMovementQueryRequest;
import com.commerce.platform.inventory.dto.movement.InventoryMovementResponse;
import com.commerce.platform.inventory.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 库存流水查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository movementRepository;

    @Override
    public Page<InventoryMovementResponse> listMovements(Long merchantId, InventoryMovementQueryRequest query) {
        // Merchant 端 - 按指定 Inventory 过滤（需要与 Service 层结合）
        // 当前版本：全量分页查询，后续与 Inventory 关联实现 merchant_id 过滤
        return queryAll(query);
    }

    @Override
    public Page<InventoryMovementResponse> listAllMovements(InventoryMovementQueryRequest query) {
        return queryAll(query);
    }

    @Override
    public InventoryMovementDetailResponse getMovementDetail(Long movementId) {
        InventoryMovement movement = movementRepository.findById(movementId)
                .orElseThrow(() -> new BusinessException("流水记录不存在：" + movementId));

        return toDetailResponse(movement);
    }

    @Override
    public void exportMovements(InventoryMovementQueryRequest query) {
        // 预留：后续 Sprint 实现 Excel 导出
        log.info("导出库存流水：预留功能，未实现");
    }

    // ========== 私有方法 ==========

    private Page<InventoryMovementResponse> queryAll(InventoryMovementQueryRequest query) {
        PageRequest pageRequest = PageRequest.of(
                query.getPage() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );
        Page<InventoryMovement> page = movementRepository.findAll(pageRequest);
        return page.map(this::toResponse);
    }

    private InventoryMovementResponse toResponse(InventoryMovement m) {
        InventoryMovementResponse r = new InventoryMovementResponse();
        r.setMovementNo(m.getMovementNo());
        r.setProductSkuId(m.getProductSkuId());
        r.setMovementType(m.getMovementType().name());
        r.setSourceType(m.getSourceType() != null ? m.getSourceType().name() : null);
        r.setSourceId(m.getSourceId());
        r.setReasonCode(m.getReasonCode() != null ? m.getReasonCode().name() : null);
        r.setQuantity(m.getQuantity());
        r.setBeforeAvailable(m.getBeforeAvailable());
        r.setAfterAvailable(m.getAfterAvailable());
        r.setOperatorName(m.getOperatorName());
        r.setRemark(m.getRemark());
        r.setCreatedTime(m.getCreatedTime());
        return r;
    }

    private InventoryMovementDetailResponse toDetailResponse(InventoryMovement m) {
        InventoryMovementDetailResponse r = new InventoryMovementDetailResponse();
        r.setMovementNo(m.getMovementNo());
        r.setProductSkuId(m.getProductSkuId());
        r.setInventoryId(m.getInventoryId());
        r.setMovementType(m.getMovementType().name());
        r.setSourceType(m.getSourceType() != null ? m.getSourceType().name() : null);
        r.setSourceId(m.getSourceId());
        r.setReasonCode(m.getReasonCode() != null ? m.getReasonCode().name() : null);
        r.setQuantity(m.getQuantity());
        r.setBeforeAvailable(m.getBeforeAvailable());
        r.setAfterAvailable(m.getAfterAvailable());
        r.setBeforeReserved(m.getBeforeReserved());
        r.setAfterReserved(m.getAfterReserved());
        r.setOperatorId(m.getOperatorId());
        r.setOperatorName(m.getOperatorName());
        r.setBusinessId(m.getBusinessId());
        r.setRemark(m.getRemark());
        r.setCreatedTime(m.getCreatedTime());
        return r;
    }
}