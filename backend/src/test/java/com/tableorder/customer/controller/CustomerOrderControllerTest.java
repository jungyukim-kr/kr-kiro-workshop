package com.tableorder.customer.controller;

import com.tableorder.auth.dto.AuthInfo;
import com.tableorder.customer.dto.*;
import com.tableorder.customer.exception.CustomerException;
import com.tableorder.customer.service.CustomerOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOrderControllerTest {

    @Mock private CustomerOrderService orderService;
    @InjectMocks private CustomerOrderController controller;

    // === TC-CUST-022: POST /orders → 201 Created ===
    @Test
    @DisplayName("TC-CUST-022: POST /orders → 201 Created")
    void createOrder_201() {
        Authentication auth = createAuth(5L, 1L, 10L);
        OrderResponse resp = new OrderResponse();
        resp.setOrderId(42L);
        resp.setOrderNumber("20260306-001");
        resp.setTotalAmount(18000);
        resp.setStatus("WAITING");
        resp.setSessionId(10L);
        when(orderService.createOrder(eq(1L), eq(5L), eq(10L), any())).thenReturn(resp);

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuId(1L);
        item.setQuantity(2);
        item.setUnitPrice(9000);
        request.setItems(List.of(item));

        ResponseEntity<OrderResponse> result = controller.createOrder(1L, auth, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().getOrderId()).isEqualTo(42L);
    }

    // === TC-CUST-023: POST /orders → 400 빈 items (exception from service) ===
    @Test
    @DisplayName("TC-CUST-023: POST /orders → VALIDATION_FAILED 에러 전파")
    void createOrder_validationFailed() {
        Authentication auth = createAuth(5L, 1L, 10L);
        when(orderService.createOrder(eq(1L), eq(5L), eq(10L), any()))
                .thenThrow(new CustomerException("VALIDATION_FAILED", "주문 항목이 비어있습니다", HttpStatus.BAD_REQUEST));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(Collections.emptyList());

        assertThatThrownBy(() -> controller.createOrder(1L, auth, request))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("VALIDATION_FAILED"));
    }

    // === TC-CUST-024: POST /orders → 409 PRICE_MISMATCH ===
    @Test
    @DisplayName("TC-CUST-024: POST /orders → 409 PRICE_MISMATCH")
    void createOrder_priceMismatch() {
        Authentication auth = createAuth(5L, 1L, 10L);
        when(orderService.createOrder(eq(1L), eq(5L), eq(10L), any()))
                .thenThrow(new CustomerException("PRICE_MISMATCH", "메뉴 가격이 변경되었습니다", HttpStatus.CONFLICT));

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuId(1L);
        item.setQuantity(1);
        item.setUnitPrice(8000);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> controller.createOrder(1L, auth, request))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("PRICE_MISMATCH"));
    }

    // === TC-CUST-025: GET /orders → 200 OK 페이지네이션 ===
    @Test
    @DisplayName("TC-CUST-025: GET /orders → 200 OK 페이지네이션")
    void getOrders_200() {
        Authentication auth = createAuth(5L, 1L, 10L);
        OrderListResponse resp = new OrderListResponse();
        resp.setOrders(List.of(new OrderResponse(), new OrderResponse()));
        resp.setPage(0);
        resp.setSize(10);
        resp.setTotalElements(2);
        resp.setTotalPages(1);
        when(orderService.getOrders(10L, 0, 10)).thenReturn(resp);

        ResponseEntity<OrderListResponse> result = controller.getOrders(1L, auth, 0, 10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getOrders()).hasSize(2);
        assertThat(result.getBody().getTotalElements()).isEqualTo(2);
    }

    private Authentication createAuth(Long tableId, Long storeId, Long sessionId) {
        AuthInfo authInfo = new AuthInfo(tableId, storeId, "TABLE", tableId, sessionId);
        return new UsernamePasswordAuthenticationToken(authInfo, null,
                List.of(new SimpleGrantedAuthority("ROLE_TABLE")));
    }
}
