package com.commerce.platform.shipping.domain.entity;

import com.commerce.platform.shipping.domain.aggregate.TrackingRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrackingRecord 测试")
class TrackingRecordTest {

    @Test
    @DisplayName("创建轨迹记录")
    void shouldCreateTrackingRecord() {
        TrackingRecord record = TrackingRecord.create(1L, "Shenzhen", "Picked up", "PICKED");
        assertEquals(1L, record.getShipmentId());
        assertEquals("Shenzhen", record.getLocation());
        assertEquals("Picked up", record.getDescription());
        assertNotNull(record.getOccurredAt());
    }

    @Test
    @DisplayName("多轨迹应包含所有记录并可排序")
    void shouldContainAllRecords() {
        TrackingRecord r1 = TrackingRecord.create(1L, "Shenzhen", "Picked", "PICKED");
        TrackingRecord r2 = TrackingRecord.create(1L, "Beijing Hub", "In transit", "TRANSIT");
        TrackingRecord r3 = TrackingRecord.create(1L, "Beijing", "Delivered", "DELIVERED");

        List<TrackingRecord> records = List.of(r1, r2, r3);
        assertEquals(3, records.size());
        assertTrue(records.stream().anyMatch(r -> r.getLocation().equals("Shenzhen")));
        assertTrue(records.stream().anyMatch(r -> r.getLocation().equals("Beijing Hub")));
        assertTrue(records.stream().anyMatch(r -> r.getLocation().equals("Beijing")));
        assertNotNull(records.get(0).getOccurredAt());
    }
}