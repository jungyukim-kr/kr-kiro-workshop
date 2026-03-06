package com.tableorder.customer.controller;

import com.tableorder.auth.dto.AuthInfo;
import com.tableorder.customer.dto.CreateOrderRequest;
import com.tableorder.customer.dto.OrderListResponse;
import com.tableorder.customer.dto.OrderResponse;
import com.tableorder.customer.service.CustomerOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores/{storeId}/customer")
public class CustomerOrderController {
    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long storeId,
                                                      Authentication authentication,
                                                      @Valid @RequestBody CreateOrderRequest request) {
        AuthInfo auth = (AuthInfo) authentication.getPrincipal();
        OrderResponse response = orderService.createOrder(storeId, auth.getTableId(), auth.getSessionId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderListResponse> getOrders(@PathVariable Long storeId,
                                                        Authentication authentication,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        AuthInfo auth = (AuthInfo) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getOrders(auth.getSessionId(), page, size));
    }
}
