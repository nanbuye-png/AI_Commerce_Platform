package com.commerce.platform.inventory.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.domain.entity.InventoryMovement;
import com.commerce.platform.inventory.domain.enums.MovementType;
import com.commerce.platform.inventory.domain.repository.InventoryMovementRepository;
import com.commerce.platform.inventory.dto.merchant.*;
import com.commerce.platform.inventory.service.InventoryService;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 库存服务实现
 * <p>
 * Sprint 21 Step 2B: Migrated from Inventory Entity to InventoryStock Aggregate.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    public Page<InventoryListResponse> listInventory(Long merchantId, InventoryQueryRequest query) {
        PageRequest pageRequest = PageRequest.of(
                query.getPage() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedTime")
        );
        // findAll returns List; convert to Page for backward compat
        // Note: This is a simplification - pagination is now in-memory
        var all = inventoryStockRepository.findAll();
        // Use simple offset-based pagination
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), all.size());
        if (start >= all.size()) return Page.empty();

        var pageContent = all.subList(start, end);
        var mapped = pageContent.stream().map(stock -> {
            InventoryListResponse resp = new InventoryListResponse();
            resp.setId(stock.getId());
            resp.setProductSkuId(stock.getSkuId());
            resp.setAvailableStock(stock.getAvailableQuantity());
            resp.setReservedStock(stock.getReservedQuantity());
            resp.setTotalStock(stock.getTotalQuantity() + stock.getSoldQuantity());
            resp.setLowStock(stock.getAvailableQuantity() <= 0);
            return resp;
        }).toList();

        return new org.springframework.data.domain.PageImpl<>(mapped, pageRequest, all.size());
    }

    @Override
    public InventoryDetailResponse getInventoryDetail(Long merchantId, Long inventoryId) {
        InventoryStock stock = findInventoryStockById(inventoryId);

        InventoryDetailResponse resp = new InventoryDetailResponse();
        resp.setId(stock.getId());
        resp.setProductSkuId(stock.getSkuId());
        resp.setAvailableStock(stock.getAvailableQuantity());
        resp.setReservedStock(stock.getReservedQuantity());
        resp.setTotalStock(stock.getTotalQuantity() + stock.getSoldQuantity());
        resp.setLowStockThreshold(0);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustInventory(Long merchantId, Long inventoryId, InventoryAdjustRequest request) {
        InventoryStock stock = findInventoryStockById(inventoryId);
        int quantity = request.getQuantity();
        int beforeAvailable = stock.getAvailableQuantity();

        switch (request.getAdjustType().toUpperCase()) {
            case "INCREASE":
                // Sprint 21 Step 2B: use adjust() instead of setAvailableStock()
                stock.adjust(quantity);
                break;
            case "DECREASE":
                if (stock.getAvailableQuantity() < quantity) {
                    throw new BusinessException("库存不足：当前可售库存 " + stock.getAvailableQuantity()
                            + "，需减少 " + quantity);
                }
                stock.adjust(-quantity);
                break;
            default:
                throw new BusinessException("不支持的调整类型：" + request.getAdjustType()
                        + "，仅支持 INCREASE / DECREASE");
        }

        inventoryStockRepository.save(stock);

        createMovement(stock, MovementType.ADJUST, quantity, beforeAvailable,
                stock.getAvailableQuantity(), request.getRemark(), merchantId);

        log.info("商家 {} 调整库存 ID={}，调整类型={}，数量={}，剩余可售={}",
                merchantId, inventoryId, request.getAdjustType(), quantity, stock.getAvailableQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundInventory(Long merchantId, Long inventoryId, InventoryAdjustRequest request) {
        InventoryStock stock = findInventoryStockById(inventoryId);
        int quantity = request.getQuantity();
        int beforeAvailable = stock.getAvailableQuantity();

        // Sprint 21 Step 2B: use inbound() instead of setAvailableStock()
        stock.inbound(quantity);
        inventoryStockRepository.save(stock);

        createMovement(stock, MovementType.INBOUND, quantity, beforeAvailable,
                stock.getAvailableQuantity(), request.getRemark(), merchantId);

        log.info("商家 {} 入库 SKU ID={}，数量={}，当前可售={}",
                merchantId, stock.getSkuId(), quantity, stock.getAvailableQuantity());
    }

    @Override
    public Page<InventoryMovementResponse> listInventoryMovements(Long merchantId, Long inventoryId,
                                                                     int page, int pageSize) {
        findInventoryStockById(inventoryId);

        PageRequest pageRequest = PageRequest.of(
                page - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdTime")
        );
        Page<InventoryMovement> movements = inventoryMovementRepository.findAll(pageRequest);

        return movements.map(m -> {
            InventoryMovementResponse resp = new InventoryMovementResponse();
            resp.setMovementNo(m.getMovementNo());
            resp.setProductSkuId(m.getProductSkuId());
            resp.setMovementType(m.getMovementType().name());
            resp.setQuantity(m.getQuantity());
            resp.setBeforeAvailable(m.getBeforeAvailable());
            resp.setAfterAvailable(m.getAfterAvailable());
            resp.setOperatorId(m.getOperatorId());
            resp.setRemark(m.getRemark());
            resp.setCreatedTime(m.getCreatedTime());
            return resp;
        });
    }

    private InventoryStock findInventoryStockById(Long inventoryId) {
        return inventoryStockRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException("库存记录不存在：" + inventoryId));
    }

    private void createMovement(InventoryStock stock, MovementType movementType,
                                 int quantity, int beforeAvailable, int afterAvailable,
                                 String remark, Long operatorId) {
        InventoryMovement movement = InventoryMovement.builder()
                .movementNo(generateMovementNo())
                .productSkuId(stock.getSkuId())
                .inventoryId(stock.getId())
                .movementType(movementType)
                .quantity(quantity)
                .beforeAvailable(beforeAvailable)
                .afterAvailable(afterAvailable)
                .operatorId(operatorId)
                .remark(remark)
                .build();
        inventoryMovementRepository.save(movement);
    }

    private String generateMovementNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "MV" + timestamp + random;
    }
}