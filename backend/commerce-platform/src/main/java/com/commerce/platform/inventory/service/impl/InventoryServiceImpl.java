package com.commerce.platform.inventory.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.domain.entity.Inventory;
import com.commerce.platform.inventory.domain.entity.InventoryMovement;
import com.commerce.platform.inventory.domain.enums.MovementType;
import com.commerce.platform.inventory.domain.repository.InventoryMovementRepository;
import com.commerce.platform.inventory.domain.repository.InventoryRepository;
import com.commerce.platform.inventory.dto.merchant.*;
import com.commerce.platform.inventory.service.InventoryService;
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
 * 核心业务规则：
 * 1. 三字段模型：totalStock = availableStock + reservedStock（禁止直接修改 totalStock）
 * 2. 所有库存变更必须生成 InventoryMovement（Append-Only）
 * 3. 不允许负库存（availableStock >= 0, reservedStock >= 0, totalStock >= 0）
 * 4. 所有修改操作在同一事务中完成
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    public Page<InventoryListResponse> listInventory(Long merchantId, InventoryQueryRequest query) {
        // 当前版本：简单分页查询所有库存记录
        // 后续可扩展：根据 merchantId 关联 product_sku 表做数据隔离
        PageRequest pageRequest = PageRequest.of(
                query.getPage() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedTime")
        );
        Page<Inventory> page = inventoryRepository.findAll(pageRequest);

        return page.map(inventory -> {
            InventoryListResponse resp = new InventoryListResponse();
            resp.setId(inventory.getId());
            resp.setProductSkuId(inventory.getSkuId());
            resp.setAvailableStock(inventory.getAvailableStock());
            resp.setReservedStock(inventory.getLockedStock());
            resp.setTotalStock(inventory.getAvailableStock() + inventory.getLockedStock() + inventory.getSoldStock());
            resp.setLowStock(inventory.getAvailableStock() <= 0);
            return resp;
        });
    }

    @Override
    public InventoryDetailResponse getInventoryDetail(Long merchantId, Long inventoryId) {
        Inventory inventory = findInventoryById(inventoryId);

        InventoryDetailResponse resp = new InventoryDetailResponse();
        resp.setId(inventory.getId());
        resp.setProductSkuId(inventory.getSkuId());
        resp.setAvailableStock(inventory.getAvailableStock());
        resp.setReservedStock(inventory.getLockedStock());
        resp.setTotalStock(inventory.getAvailableStock() + inventory.getLockedStock() + inventory.getSoldStock());
        resp.setLowStockThreshold(0);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustInventory(Long merchantId, Long inventoryId, InventoryAdjustRequest request) {
        Inventory inventory = findInventoryById(inventoryId);
        int quantity = request.getQuantity();
        int beforeAvailable = inventory.getAvailableStock();

        switch (request.getAdjustType().toUpperCase()) {
            case "INCREASE":
                // 增加库存：仅增加 availableStock，totalStock 自动计算
                inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
                break;
            case "DECREASE":
                // 减少库存：校验可用库存是否充足
                if (inventory.getAvailableStock() < quantity) {
                    throw new BusinessException("库存不足：当前可售库存 " + inventory.getAvailableStock()
                            + "，需减少 " + quantity);
                }
                inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
                break;
            default:
                throw new BusinessException("不支持的调整类型：" + request.getAdjustType()
                        + "，仅支持 INCREASE / DECREASE");
        }

        inventoryRepository.save(inventory);

        // 生成库存流水
        createMovement(inventory, MovementType.ADJUST, quantity, beforeAvailable,
                inventory.getAvailableStock(), request.getRemark(), merchantId);

        log.info("商家 {} 调整库存 ID={}，调整类型={}，数量={}，剩余可售={}",
                merchantId, inventoryId, request.getAdjustType(), quantity, inventory.getAvailableStock());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundInventory(Long merchantId, Long inventoryId, InventoryAdjustRequest request) {
        Inventory inventory = findInventoryById(inventoryId);
        int quantity = request.getQuantity();
        int beforeAvailable = inventory.getAvailableStock();

        // 入库仅支持增加库存
        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventoryRepository.save(inventory);

        // 生成入库流水
        createMovement(inventory, MovementType.INBOUND, quantity, beforeAvailable,
                inventory.getAvailableStock(), request.getRemark(), merchantId);

        log.info("商家 {} 入库 SKU ID={}，数量={}，当前可售={}",
                merchantId, inventory.getSkuId(), quantity, inventory.getAvailableStock());
    }

    @Override
    public Page<InventoryMovementResponse> listInventoryMovements(Long merchantId, Long inventoryId,
                                                                    int page, int pageSize) {
        // 验证库存记录存在
        findInventoryById(inventoryId);

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

    // ========== 私有方法 ==========

    /**
     * 根据 ID 查找库存记录，不存在则抛出异常
     */
    private Inventory findInventoryById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException("库存记录不存在：" + inventoryId));
    }

    /**
     * 创建库存流水记录（Append-Only，仅 INSERT）
     *
     * @param inventory      库存实体
     * @param movementType   变动类型
     * @param quantity       变动数量
     * @param beforeAvailable 变动前可售库存
     * @param afterAvailable  变动后可售库存
     * @param remark         备注
     * @param operatorId     操作人 ID
     */
    private void createMovement(Inventory inventory, MovementType movementType,
                                 int quantity, int beforeAvailable, int afterAvailable,
                                 String remark, Long operatorId) {
        InventoryMovement movement = InventoryMovement.builder()
                .movementNo(generateMovementNo())
                .productSkuId(inventory.getSkuId())
                .inventoryId(inventory.getId())
                .movementType(movementType)
                .quantity(quantity)
                .beforeAvailable(beforeAvailable)
                .afterAvailable(afterAvailable)
                .operatorId(operatorId)
                .remark(remark)
                .build();
        inventoryMovementRepository.save(movement);
    }

    /**
     * 生成流水编号
     * 格式：MV + yyyyMMddHHmmss + 6位随机字符
     */
    private String generateMovementNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "MV" + timestamp + random;
    }
}