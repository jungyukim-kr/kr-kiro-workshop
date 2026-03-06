package com.tableorder.customer.repository;

import com.tableorder.common.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}
