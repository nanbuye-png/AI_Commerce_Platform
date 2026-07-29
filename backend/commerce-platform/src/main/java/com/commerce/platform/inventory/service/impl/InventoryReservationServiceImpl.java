package com.commerce.platform.inventory.service.impl;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.domain.entity.InventoryMovement;
import com.commerce.platform.inventory.domain.entity.InventoryReservation;
import com.commerce.platform.inventory.domain.enums.MovementType;
import com.commerce.platform.inventory.domain.enums.ReservationStatus;
import com.commerce.platform.inventory.domain.repository.InventoryMovementRepository;
import com.commerce.platform.inventory.domain.repository.InventoryReservationRepository;
import com.commerce.platform.inventory.dto.reservation.*;
import com.commerce.platform.inventory.mq.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.mq.event.InventoryReleasedEvent;
import com.commerce.platform.inventory.mq.event.InventoryReservedEvent;
import com.commerce.platform.inventory.service.InventoryReservationService;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
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
 * Sprint 21 Step 2B: Inventory management migrated to InventoryStock Aggregate + InventoryStockRepository.
 * InventoryReservation (domain/entity) retained for reservationNo-based queries (no direct StockReservation equivalent).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryMovementRepository movementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse reserve(ReserveInventoryRequest request) {
        InventoryStock stock = findStockById(request.getInventoryId());

        if (stock.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException("库存不足：当前可售库存 " + stock.getAvailableQuantity()
                    + "，需求 " + request.getQuantity());
        }

        String reservationNo = generateReservationNo();

        // Sprint 21 Step 2B: use reserve() instead of setAvailableStock()/setLockedStock()
        int beforeAvailable = stock.getAvailableQuantity();
        stock.reserve(request.getQuantity());
        inventoryStockRepository.save(stock);

        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(request.getExpireMinutes());
        InventoryReservation reservation = InventoryReservation.builder()
                .reservationNo(reservationNo)
                .inventoryId(stock.getId())
                .productSkuId(request.getProductSkuId())
                .orderId(request.getOrderId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.ACTIVE)
                .expireTime(expireTime)
                .build();
        reservationRepository.save(reservation);

        createMovement(stock, MovementType.RESERVE, request.getQuantity(), beforeAvailable, stock.getAvailableQuantity());

        eventPublisher.publishEvent(new InventoryReservedEvent(
                reservationNo, stock.getId(), request.getProductSkuId(),
                request.getOrderId(), request.getQuantity()));

        log.info("库存锁定成功：reservationNo={}, skuId={}, orderId={}, quantity={}",
                reservationNo, request.getProductSkuId(), request.getOrderId(), request.getQuantity());

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
        InventoryReservation reservation = reservationRepository.findByReservationNo(request.getReservationNo())
                .orElseThrow(() -> new BusinessException("预占记录不存在：" + request.getReservationNo()));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException("预占状态不合法：当前状态 " + reservation.getStatus()
                    + "，仅 ACTIVE 可释放");
        }

        if (request.getQuantity() > reservation.getQuantity()) {
            throw new BusinessException("释放数量超过预占数量：预占 " + reservation.getQuantity()
                    + "，释放 " + request.getQuantity());
        }

        InventoryStock stock = findStockById(reservation.getInventoryId());

        int beforeAvailable = stock.getAvailableQuantity();
        // Sprint 21 Step 2B: use release() instead of setLockedStock()/setAvailableStock()
        stock.release(request.getQuantity());
        inventoryStockRepository.save(stock);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        createMovement(stock, MovementType.RELEASE, request.getQuantity(), beforeAvailable, stock.getAvailableQuantity());

        eventPublisher.publishEvent(new InventoryReleasedEvent(
                request.getReservationNo(), stock.getId(), reservation.getProductSkuId(),
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
        InventoryReservation reservation = reservationRepository.findByReservationNo(request.getReservationNo())
                .orElseThrow(() -> new BusinessException("预占记录不存在：" + request.getReservationNo()));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException("预占状态不合法：当前状态 " + reservation.getStatus()
                    + "，仅 ACTIVE 可扣减");
        }

        if (request.getQuantity() > reservation.getQuantity()) {
            throw new BusinessException("扣减数量超过预占数量：预占 " + reservation.getQuantity()
                    + "，扣减 " + request.getQuantity());
        }

        InventoryStock stock = findStockById(reservation.getInventoryId());

        int beforeReserved = stock.getReservedQuantity();
        // Sprint 21 Step 2B: use confirm() instead of setLockedStock()/setSoldStock()
        stock.confirm(request.getQuantity());
        inventoryStockRepository.save(stock);

        reservation.setStatus(ReservationStatus.DEDUCTED);
        reservationRepository.save(reservation);

        createMovement(stock, MovementType.DEDUCT, request.getQuantity(), beforeReserved, stock.getReservedQuantity());

        eventPublisher.publishEvent(new InventoryDeductedEvent(
                request.getReservationNo(), stock.getId(), reservation.getProductSkuId(),
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

    private InventoryStock findStockById(Long inventoryId) {
        return inventoryStockRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException("库存记录不存在：" + inventoryId));
    }

    private void createMovement(InventoryStock stock, MovementType movementType,
                                 int quantity, int beforeValue, int afterValue) {
        InventoryMovement movement = InventoryMovement.builder()
                .movementNo(generateMovementNo())
                .productSkuId(stock.getSkuId())
                .inventoryId(stock.getId())
                .movementType(movementType)
                .quantity(quantity)
                .beforeAvailable(beforeValue)
                .afterAvailable(afterValue)
                .build();
        movementRepository.save(movement);
    }

    private String generateReservationNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "RSV" + timestamp + random;
    }

    private String generateMovementNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "MV" + timestamp + random;
    }
}