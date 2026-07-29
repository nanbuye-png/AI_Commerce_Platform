package com.commerce.platform.shipping.domain.repository;

import com.commerce.platform.shipping.domain.aggregate.Shipment;
import java.util.Optional;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(Long id);
    Optional<Shipment> findByFulfillmentId(Long fulfillmentId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}