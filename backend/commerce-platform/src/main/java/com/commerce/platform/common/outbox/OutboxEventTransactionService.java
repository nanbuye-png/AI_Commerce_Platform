package com.commerce.platform.common.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventTransactionService {

    static final int MAX_RETRIES = 5;
    static final int BATCH_SIZE = 100;
    static final int PROCESSING_TIMEOUT_MINUTES = 5;

    private final OutboxRepository repository;

    /**
     * 在单个短事务内锁定并领取事件。PROCESSING 超时记录会计为一次失败后重新领取。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OutboxEvent> claimPendingEvents() {
        List<OutboxEvent> candidates = repository.findPendingForProcessing(
                EnumSet.of(OutboxStatus.NEW, OutboxStatus.FAILED),
                OutboxStatus.PROCESSING,
                MAX_RETRIES,
                LocalDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES),
                PageRequest.of(0, BATCH_SIZE));
        List<OutboxEvent> claimed = new ArrayList<>(candidates.size());

        for (OutboxEvent event : candidates) {
            if (event.getStatus() == OutboxStatus.PROCESSING) {
                event.markFailed("Processing lease expired before completion");
                if (!event.shouldRetry()) {
                    continue;
                }
            }
            event.markProcessing(UUID.randomUUID().toString());
            claimed.add(event);
        }
        repository.saveAll(candidates);
        return claimed;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long id, String processingToken) {
        repository.findByIdForUpdate(id)
                .filter(event -> event.isClaimedBy(processingToken))
                .ifPresent(OutboxEvent::markSuccess);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long id, String processingToken, String error) {
        repository.findByIdForUpdate(id)
                .filter(event -> event.isClaimedBy(processingToken))
                .ifPresent(event -> event.markFailed(error));
    }
}