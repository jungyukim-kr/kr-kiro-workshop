package com.tableorder.auth.repository;

import com.tableorder.common.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableSessionRepository extends JpaRepository<TableSession, Long> {
    Optional<TableSession> findByTableIdAndActiveTrue(Long tableId);
}
