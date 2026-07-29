package com.commerce.platform.shipping.domain.repository;

import com.commerce.platform.shipping.domain.aggregate.TrackingRecord;
import java.util.List;
import java.util.Optional;

public interface TrackingRecordRepository {
    TrackingRecord save(TrackingRecord record);
    List<TrackingRecord> findByShipmentId(Long shipmentId);
    Optional<TrackingRecord> findLatestByShipmentId(Long shipmentId);
}