package com.commerce.platform.shipping.application.handler;

import com.commerce.platform.shipping.application.command.UpdateTrackingCommand;
import com.commerce.platform.shipping.domain.aggregate.TrackingRecord;
import com.commerce.platform.shipping.domain.repository.TrackingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateTrackingHandler {

    private final TrackingRecordRepository trackingRecordRepository;

    @Transactional(rollbackFor = Exception.class)
    public TrackingRecord handle(UpdateTrackingCommand command) {
        log.info("更新物流轨迹: shipmentId={}, location={}", command.getShipmentId(), command.getLocation());

        TrackingRecord record = TrackingRecord.create(
                command.getShipmentId(), command.getLocation(),
                command.getDescription(), command.getStatus());
        TrackingRecord saved = trackingRecordRepository.save(record);

        log.info("物流轨迹已记录: trackingRecordId={}", saved.getId());
        return saved;
    }
}