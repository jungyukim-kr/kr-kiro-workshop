package com.tableorder.customer.repository;

import com.tableorder.common.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);
    long countByStoreIdAndOrderNumberStartingWith(Long storeId, String prefix);
}
