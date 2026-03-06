package com.tableorder.customer.repository;

import com.tableorder.common.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerTableSessionRepository extends JpaRepository<TableSession, Long> {
    Optional<TableSession> findByIdAndActiveTrue(Long id);
}
