package com.commerce.platform.common.outbox;

/**
 * Outbox 事件状态
 * <p>
 * NEW ──markProcessing()──→ PROCESSING ──markSuccess()──→ SUCCESS
 *                                       ──markFailed()──→ FAILED
 * </p>
 */
public enum OutboxStatus {
    NEW,
    PROCESSING,
    SUCCESS,
    FAILED
}