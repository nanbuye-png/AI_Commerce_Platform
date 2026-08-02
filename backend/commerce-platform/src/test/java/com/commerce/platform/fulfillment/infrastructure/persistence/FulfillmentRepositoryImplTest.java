package com.commerce.platform.fulfillment.infrastructure.persistence;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import com.commerce.platform.fulfillment.domain.valueobject.ShipmentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FulfillmentRepositoryImpl 映射转换测试
 * <p>
 * 测试 Domain Aggregate 与 JPA Entity 之间的双向转换逻辑。
 * 不依赖真实数据库连接。
 * </p>
 */
@DisplayName("FulfillmentRepository 映射转换测试")
class FulfillmentRepositoryImplTest {

    private FulfillmentRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new FulfillmentRepositoryImpl(null);
    }

    @Test
    @DisplayName("Domain → Entity：基础字段映射")
    void toEntityShouldMapBasicFields() {
        Fulfillment fulfillment = Fulfillment.create(1L, 100L);
        fulfillment.setId(10L);
        fulfillment.assignWarehouse(50L);

        FulfillmentEntity entity = repository.toEntity(fulfillment);

        assertEquals(10L, entity.getId());
        assertEquals(1L, entity.getOrderId());
        assertEquals(100L, entity.getMerchantId());
        assertEquals(50L, entity.getWarehouseId());
        assertEquals(FulfillmentStatus.PENDING, entity.getStatus());
    }

    @Test
    @DisplayName("Domain → Entity：包含 ShipmentInfo 映射")
    void toEntityShouldMapShipmentInfo() {
        Fulfillment fulfillment = Fulfillment.create(1L, 100L);
        fulfillment.startProcessing();
        fulfillment.startPicking();
        fulfillment.startPacking();
        fulfillment.markWaitingShipment();

        ShipmentInfo shipmentInfo = new ShipmentInfo(
                "SF Express", "SF", "SF123456",
                "Beijing, China", LocalDateTime.now().plusDays(2));
        fulfillment.ship(shipmentInfo);

        FulfillmentEntity entity = repository.toEntity(fulfillment);

        assertEquals("SF Express", entity.getCarrier());
        assertEquals("SF", entity.getCarrierCode());
        assertEquals("SF123456", entity.getTrackingNumber());
        assertEquals("Beijing, China", entity.getShippingAddress());
        assertNotNull(entity.getEstimatedArrival());
    }

    @Test
    @DisplayName("Domain → Entity：不含 ShipmentInfo 时物流字段应为空")
    void toEntityShouldHandleNullShipmentInfo() {
        Fulfillment fulfillment = Fulfillment.create(1L, 100L);

        FulfillmentEntity entity = repository.toEntity(fulfillment);

        assertNull(entity.getCarrier());
        assertNull(entity.getTrackingNumber());
    }

    @Test
    @DisplayName("Entity → Domain：基础字段恢复")
    void toDomainShouldRestoreBasicFields() {
        FulfillmentEntity entity = new FulfillmentEntity();
        entity.setId(10L);
        entity.setOrderId(1L);
        entity.setMerchantId(100L);
        entity.setWarehouseId(50L);
        entity.setStatus(FulfillmentStatus.PROCESSING);
        entity.setCreatedTime(LocalDateTime.now().minusDays(1));
        entity.setUpdatedTime(LocalDateTime.now());

        Fulfillment domain = repository.toDomain(entity);

        assertEquals(10L, domain.getId());
        assertEquals(1L, domain.getOrderId());
        assertEquals(100L, domain.getMerchantId());
        assertEquals(50L, domain.getWarehouseId());
        assertEquals(FulfillmentStatus.PROCESSING, domain.getStatus());
        assertNotNull(domain.getCreatedAt());
        assertNotNull(domain.getUpdatedAt());
    }

    @Test
    @DisplayName("Entity → Domain：包含物流信息恢复")
    void toDomainShouldRestoreShipmentInfo() {
        FulfillmentEntity entity = new FulfillmentEntity();
        entity.setId(10L);
        entity.setOrderId(1L);
        entity.setMerchantId(100L);
        entity.setStatus(FulfillmentStatus.SHIPPED);
        entity.setCarrier("YTO Express");
        entity.setCarrierCode("YTO");
        entity.setTrackingNumber("YTO987654");
        entity.setShippingAddress("Shanghai, China");
        entity.setEstimatedArrival(LocalDateTime.now().plusDays(1));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        Fulfillment domain = repository.toDomain(entity);

        assertNotNull(domain.getShipmentInfo());
        assertEquals("YTO Express", domain.getShipmentInfo().getCarrier());
        assertEquals("YTO", domain.getShipmentInfo().getCarrierCode());
        assertEquals("YTO987654", domain.getShipmentInfo().getTrackingNumber());
        assertEquals("Shanghai, China", domain.getShipmentInfo().getShippingAddress());
        assertEquals("YTO:YTO987654", domain.getShipmentInfo().getTrackingId());
    }

    @Test
    @DisplayName("Entity → Domain：不含物流信息时应为 null")
    void toDomainShouldHandleNullShipmentFields() {
        FulfillmentEntity entity = new FulfillmentEntity();
        entity.setId(10L);
        entity.setOrderId(1L);
        entity.setMerchantId(100L);
        entity.setStatus(FulfillmentStatus.PENDING);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        Fulfillment domain = repository.toDomain(entity);

        assertNull(domain.getShipmentInfo());
    }

    @Test
    @DisplayName("双向转换：Domain → Entity → Domain 应保持数据一致性")
    void shouldBeConsistentAfterRoundTrip() {
        // Original domain
        Fulfillment original = Fulfillment.create(5L, 500L);
        original.setId(20L);
        original.assignWarehouse(30L);
        original.startProcessing();
        original.startPicking();
        original.startPacking();
        original.markWaitingShipment();
        original.ship(new ShipmentInfo("ZTO", "ZTO", "ZTO111",
                "Guangzhou", LocalDateTime.now().plusDays(1)));

        // Convert to entity
        FulfillmentEntity entity = repository.toEntity(original);

        // Convert back to domain
        Fulfillment restored = repository.toDomain(entity);

        // Verify consistency
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getOrderId(), restored.getOrderId());
        assertEquals(original.getMerchantId(), restored.getMerchantId());
        assertEquals(original.getWarehouseId(), restored.getWarehouseId());
        assertEquals(original.getStatus(), restored.getStatus());
        assertNotNull(restored.getShipmentInfo());
        assertEquals(original.getShipmentInfo().getCarrier(), restored.getShipmentInfo().getCarrier());
        assertEquals(original.getShipmentInfo().getTrackingNumber(), restored.getShipmentInfo().getTrackingNumber());
    }
}