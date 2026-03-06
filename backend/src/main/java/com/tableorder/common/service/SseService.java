package com.tableorder.common.service;

import com.tableorder.customer.dto.OrderResponse;

public interface SseService {
    void publishNewOrder(Long storeId, OrderResponse order);
}
