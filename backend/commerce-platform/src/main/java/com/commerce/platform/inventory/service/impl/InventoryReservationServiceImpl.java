package com.commerce.platform.inventory.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.domain.entity.Inventory;
import com.commerce.platform.inventory.domain.entity.InventoryMovement;
import com.commerce.platform.inventory.domain.entity.InventoryReservation;
import com.commerce.platform.inventory.domain.enums.MovementType;
import com.commerce.platform.inventory.domain.enums.ReservationStatus;
import com.commerce.platform.inventory.domain.repository.InventoryMovementRepository;
import com.commerce.platform.inventory.domain.repository.InventoryRepository;
import com.commerce.platform.inventory.domain.repository.InventoryReservationRepository;
import com.commerce.platform.inventory.dto.reservation.*;
import com.commerce.platform.inventory.mq.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.mq.event.InventoryReleasedEvent;
import com.commerce.platform.inventory.mq.event.InventoryReservedEvent;
import com.commerce.platform.inventory.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 库存预占服务实现
 * <p>
 * 核心业务规则：
 * 1. Reservation 状态机：ACTIVE → RELEASED | DEDUCTED | EXPIRED（禁止逆向流转）
 * 2. 幂等性：reservationNo 唯一，重复请求不重复锁库存
 * 3. 所有操作必须生成 InventoryMovement（Append-Only）
 * 4. Inventory + Reservation + Movement 同事务提交
 * 5. Inventory 不依赖 Order Domain（orderId 仅存 Long）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryMovementRepository movementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse reserve(ReserveInventoryRequest request) {
        // 1. 查找库存记录
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new BusinessException("库存记录不存在：" + request.getInventoryId()));

        // 2. 校验可用库存
        if (inventory.getAvailableStock() < request.getQuantity()) {
            throw new BusinessException("库存不足：当前可售库存 " + inventory.getAvailableStock()
                    + "，需求 " + request.getQuantity());
        }

        // 3. 生成唯一预占编号
        String reservationNo = generateReservationNo();

        // 4. 更新库存（三字段模型）
        inventory.setAvailableStock(inventory.getAvailableStock() - request.getQuantity());
        inventory.setReservedStock(inventory.getReservedStock() + request.getQuantity());
        // totalStock 不变（totalStock = available + reserved，此处只转移不减少总量）
        inventoryRepository.save(inventory);

        // 5. 创建 Reservation 记录
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(request.getExpireMinutes());
        InventoryReservation reservation = InventoryReservation.builder()
                .reservationNo(reservationNo)
                .inventoryId(inventory.getId())
                .productSkuId(request.getProductSkuId())
                .orderId(request.getOrderId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.ACTIVE)
                .expireTime(expireTime)
                .build();
        reservationRepository.save(reservation);

        // 6. 生成库存流水
        createMovement(inventory, MovementType.RESERVE, request.getQuantity(),
                inventory.getAvailableStock() + request.getQuantity(), // 变动前
                inventory.getAvailableStock());                       // 变动后

        // 7. 发布事件
        eventPublisher.publishEvent(new InventoryReservedEvent(
                reservationNo, inventory.getId(), request.getProductSkuId(),
                request.getOrderId(), request.getQuantity()));

        log.info("库存锁定成功：reservationNo={}, skuId={}, orderId={}, quantity={}",
                reservationNo, request.getProductSkuId(), request.getOrderId(), request.getQuantity());

        // 8. 返回响应
        ReservationResponse response = new ReservationResponse();
        response.setReservationNo(reservationNo);
        response.setStatus(ReservationStatus.ACTIVE.name());
        response.setQuantity(request.getQuantity());
        response.setExpireTime(expireTime);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse release(ReleaseReservationRequest request) {
        // 1. 查找 Reservation
        InventoryReservation reservation = reservationRepository.findByReservationNo(request.getReservationNo())
                .orElseThrow(() -> new BusinessException("预占记录不存在：" + request.getReservationNo()));

        // 2. 状态机校验：仅 ACTIVE 可释放
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException("预占状态不合法：当前状态 " + reservation.getStatus()
                    + "，仅 ACTIVE 可释放");
        }

        // 3. 校验释放数量
        if (request.getQuantity() > reservation.getQuantity()) {
            throw new BusinessException("释放数量超过预占数量：预占 " + reservation.getQuantity()
                    + "，释放 " + request.getQuantity());
        }

        // 4. 查找并更新库存
        Inventory inventory = inventoryRepository.findById(reservation.getInventoryId())
                .orElseThrow(() -> new BusinessException("库存记录不存在：" + reservation.getInventoryId()));

        int beforeAvailable = inventory.getAvailableStock();
        inventory.setReservedStock(inventory.getReservedStock() - request.getQuantity());
        inventory.setAvailableStock(inventory.getAvailableStock() + request.getQuantity());
        inventoryRepository.save(inventory);

        // 5. 更新 Reservation 状态
        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        // 6. 生成流水
        createMovement(inventory, MovementType.RELEASE, request.getQuantity(),
                beforeAvailable, inventory.getAvailableStock());

        // 7. 发布事件
        eventPublisher.publishEvent(new InventoryReleasedEvent(
                request.getReservationNo(), inventory.getId(), reservation.getProductSkuId(),
                reservation.getOrderId(), request.getQuantity(), "ORDER_CANCELLED"));

        log.info("库存释放成功：reservationNo={}, quantity={}", request.getReservationNo(), request.getQuantity());

        ReservationResponse response = new ReservationResponse();
        response.setReservationNo(request.getReservationNo());
        response.setStatus(ReservationStatus.RELEASED.name());
        response.setQuantity(request.getQuantity());
        response.setExpireTime(reservation.getExpireTime());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse deduct(DeductReservationRequest request) {
        // 1. 查找 Reservation
        InventoryReservation reservation = reservationRepository.findByReservationNo(request.getReservationNo())
                .orElseThrow(() -> new BusinessException("预占记录不存在：" + request.getReservationNo()));

        // 2. 状态机校验：仅 ACTIVE 可扣减
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException("预占状态不合法：当前状态 " + reservation.getStatus()
                    + "，仅 ACTIVE 可扣减");
        }

        // 3. 校验扣减数量
        if (request.getQuantity() > reservation.getQuantity()) {
            throw new BusinessException("扣减数量超过预占数量：预占 " + reservation.getQuantity()
                    + "，扣减 " + request.getQuantity());
        }

        // 4. 查找并更新库存（扣减减少 reserved + total）
        Inventory inventory = inventoryRepository.findById(reservation.getInventoryId())
                .orElseThrow(() -> new BusinessException("库存记录不存在：" + reservation.getInventoryId()));

        int beforeReserved = inventory.getReservedStock();
        inventory.setReservedStock(inventory.getReservedStock() - request.getQuantity());
        inventory.setTotalStock(inventory.getTotalStock() - request.getQuantity());
        // availableStock 不变（已在 reserve 时减少）
        inventoryRepository.save(inventory);

        // 5. 更新 Reservation 状态
        reservation.setStatus(ReservationStatus.DEDUCTED);
        reservationRepository.save(reservation);

        // 6. 生成流水
        createMovement(inventory, MovementType.DEDUCT, request.getQuantity(),
                beforeReserved, inventory.getReservedStock());

        // 7. 发布事件
        eventPublisher.publishEvent(new InventoryDeductedEvent(
                request.getReservationNo(), inventory.getId(), reservation.getProductSkuId(),
                reservation.getOrderId(), request.getQuantity()));

        log.info("库存扣减成功：reservationNo={}, quantity={}", request.getReservationNo(), request.getQuantity());

        ReservationResponse response = new ReservationResponse();
        response.setReservationNo(request.getReservationNo());
        response.setStatus(ReservationStatus.DEDUCTED.name());
        response.setQuantity(request.getQuantity());
        response.setExpireTime(reservation.getExpireTime());
        return response;
    }

    @Override
    public ReservationDetailResponse getReservation(String reservationNo) {
        InventoryReservation reservation = reservationRepository.findByReservationNo(reservationNo)
                .orElseThrow(() -> new BusinessException("预占记录不存在：" + reservationNo));

        ReservationDetailResponse resp = new ReservationDetailResponse();
        resp.setReservationNo(reservation.getReservationNo());
        resp.setInventoryId(reservation.getInventoryId());
        resp.setProductSkuId(reservation.getProductSkuId());
        resp.setOrderId(reservation.getOrderId());
        resp.setQuantity(reservation.getQuantity());
        resp.setStatus(reservation.getStatus().name());
        resp.setExpireTime(reservation.getExpireTime());
        resp.setCreatedTime(reservation.getCreatedTime());
        return resp;
    }

    @Override
    public Page<ReservationDetailResponse> listReservations(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(
                page - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdTime")
        );
        Page<InventoryReservation> reservations = reservationRepository.findAll(pageRequest);

        return reservations.map(r -> {
            ReservationDetailResponse resp = new ReservationDetailResponse();
            resp.setReservationNo(r.getReservationNo());
            resp.setInventoryId(r.getInventoryId());
            resp.setProductSkuId(r.getProductSkuId());
            resp.setOrderId(r.getOrderId());
            resp.setQuantity(r.getQuantity());
            resp.setStatus(r.getStatus().name());
            resp.setExpireTime(r.getExpireTime());
            resp.setCreatedTime(r.getCreatedTime());
            return resp;
        });
    }

    // ========== 私有方法 ==========

    /**
     * 创建库存流水记录（Append-Only）
     */
    private void createMovement(Inventory inventory, MovementType movementType,
                                 int quantity, int beforeValue, int afterValue) {
        InventoryMovement movement = InventoryMovement.builder()
                .movementNo(generateMovementNo())
                .productSkuId(inventory.getProductSkuId())
                .inventoryId(inventory.getId())
                .movementType(movementType)
                .quantity(quantity)
                .beforeAvailable(beforeValue)
                .afterAvailable(afterValue)
                .build();
        movementRepository.save(movement);
    }

    /**
     * 生成预占编号
     * 格式：RSV + yyyyMMddHHmmss + 6位随机字符
     */
    private String generateReservationNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "RSV" + timestamp + random;
    }

    /**
     * 生成流水编号
     */
    private String generateMovementNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "MV" + timestamp + random;
    }
}