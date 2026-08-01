package com.commerce.platform.common.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps scheduling outside the transactional processor so Spring can apply
 * the processor's transaction proxy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventProcessor processor;

    @Scheduled(fixedDelayString = "${commerce.outbox.poll-interval-ms:5000}")
    public void processPendingEvents() {
        try {
            processor.processPendingEvents();
        } catch (Exception exception) {
            log.error("Outbox batch processing failed", exception);
        }
    }
}