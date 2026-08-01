package com.commerce.platform.common.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatus(OutboxStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event from OutboxEvent event
            where (event.status in :statuses and coalesce(event.retryCount, 0) < :maxRetries)
               or (event.status = :processingStatus
                   and coalesce(event.retryCount, 0) < :maxRetries
                   and (event.processedTime is null or event.processedTime < :staleBefore))
            order by event.createdTime asc, event.id asc
            """)
    List<OutboxEvent> findPendingForProcessing(
            @Param("statuses") Collection<OutboxStatus> statuses,
            @Param("processingStatus") OutboxStatus processingStatus,
            @Param("maxRetries") int maxRetries,
            @Param("staleBefore") LocalDateTime staleBefore,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from OutboxEvent event where event.id = :id")
    Optional<OutboxEvent> findByIdForUpdate(@Param("id") Long id);

    Optional<OutboxEvent> findByEventId(String eventId);
}