package com.tableorder.common.service;

import com.tableorder.customer.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoOpSseService implements SseService {
    private static final Logger log = LoggerFactory.getLogger(NoOpSseService.class);

    @Override
    public void publishNewOrder(Long storeId, OrderResponse order) {
        log.info("SSE NEW_ORDER event (no-op): storeId={}, orderNumber={}", storeId, order.getOrderNumber());
    }
}
