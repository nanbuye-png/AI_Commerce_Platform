-- ============================================================
-- V17: Create outbox_event table
-- Description: Outbox Pattern 事件表，用于可靠投递领域事件。
-- ============================================================

CREATE TABLE IF NOT EXISTS outbox_event (
    id              BIGSERIAL                   PRIMARY KEY,
    event_id        VARCHAR(64)                 NOT NULL,
    event_type      VARCHAR(255)                NOT NULL,
    aggregate_type  VARCHAR(100),
    aggregate_id    VARCHAR(100),
    payload         TEXT                        NOT NULL,
    status          VARCHAR(20)                 NOT NULL DEFAULT 'NEW',
    retry_count     INTEGER                     DEFAULT 0,
    created_time    TIMESTAMP WITHOUT TIME ZONE,
    processed_time  TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_outbox_event_event_id UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_outbox_status
    ON outbox_event (status);

CREATE INDEX IF NOT EXISTS idx_outbox_event_id
    ON outbox_event (event_id);

-- ============================================================
-- processed_event 用于事件消费幂等
-- ============================================================

CREATE TABLE IF NOT EXISTS processed_event (
    id              BIGSERIAL                   PRIMARY KEY,
    event_id        VARCHAR(64)                 NOT NULL,
    consumer_name   VARCHAR(255)                NOT NULL,
    processed_time  TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_processed_event UNIQUE (event_id, consumer_name)
);

CREATE INDEX IF NOT EXISTS idx_processed_event_id
    ON processed_event (event_id);