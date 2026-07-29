package com.commerce.platform.warehouse.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface PackingTaskJpaRepository extends JpaRepository<PackingTaskEntity, Long> {

    Optional<PackingTaskEntity> findByPickingTaskId(Long pickingTaskId);
}