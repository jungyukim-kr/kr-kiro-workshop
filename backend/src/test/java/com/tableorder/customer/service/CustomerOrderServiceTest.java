package com.tableorder.customer.service;

import com.tableorder.common.entity.*;
import com.tableorder.common.service.SseService;
import com.tableorder.customer.dto.*;
import com.tableorder.customer.exception.CustomerException;
import com.tableorder.customer.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceTest {

    @Mock private MenuRepository menuRepository;
    @Mock private MenuSpicyOptionRepository menuSpicyOptionRepository;
    @Mock private CustomerTableSessionRepository tableSessionRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private SseService sseService;
    @InjectMocks private CustomerOrderService service;

    // === TC-CUST-008: 정상 주문 생성 (세션 존재) ===
    @Test
    @DisplayName("TC-CUST-008: 정상 주문 생성 (세션 존재)")
    void createOrder_success_existingSession() {
        TableSession session = createSession(10L, true);
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(session));
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(orderRepository.countByStoreIdAndOrderNumberStartingWith(eq(1L), anyString())).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(42L);
            return o;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = createOrderRequest(1L, 2, 9000, null, null);
        OrderResponse result = service.createOrder(1L, 5L, 10L, request);

        assertThat(result.getOrderId()).isEqualTo(42L);
        assertThat(result.getTotalAmount()).isEqualTo(18000);
        assertThat(result.getStatus()).isEqualTo("WAITING");
        assertThat(result.getSessionId()).isEqualTo(10L);
        assertThat(result.getOrderNumber()).matches("\\d{8}-\\d{3}");
    }

    // === TC-CUST-009: 세션 자동 생성 (sessionId == null) ===
    @Test
    @DisplayName("TC-CUST-009: 세션 자동 생성 (sessionId == null)")
    void createOrder_autoCreateSession() {
        when(tableSessionRepository.save(any(TableSession.class))).thenAnswer(inv -> {
            TableSession s = inv.getArgument(0);
            s.setId(20L);
            return s;
        });
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(orderRepository.countByStoreIdAndOrderNumberStartingWith(eq(1L), anyString())).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(43L);
            return o;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = createOrderRequest(1L, 1, 9000, null, null);
        OrderResponse result = service.createOrder(1L, 5L, null, request);

        assertThat(result.getSessionId()).isEqualTo(20L);
        verify(tableSessionRepository).save(any(TableSession.class));
    }

    // === TC-CUST-010: 메뉴 미존재 → MENU_NOT_FOUND ===
    @Test
    @DisplayName("TC-CUST-010: 메뉴 미존재 → MENU_NOT_FOUND")
    void createOrder_menuNotFound() {
        TableSession session = createSession(10L, true);
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(session));
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        CreateOrderRequest request = createOrderRequest(999L, 1, 9000, null, null);

        assertThatThrownBy(() -> service.createOrder(1L, 5L, 10L, request))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("MENU_NOT_FOUND"));
    }

    // === TC-CUST-011: 가격 불일치 → PRICE_MISMATCH ===
    @Test
    @DisplayName("TC-CUST-011: 가격 불일치 → PRICE_MISMATCH")
    void createOrder_priceMismatch() {
        TableSession session = createSession(10L, true);
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(session));
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        CreateOrderRequest request = createOrderRequest(1L, 1, 8000, null, null); // 가격 불일치

        assertThatThrownBy(() -> service.createOrder(1L, 5L, 10L, request))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("PRICE_MISMATCH"));
    }

    // === TC-CUST-012: 잘못된 맵기 옵션 → INVALID_SPICY_OPTION ===
    @Test
    @DisplayName("TC-CUST-012: 잘못된 맵기 옵션 → INVALID_SPICY_OPTION")
    void createOrder_invalidSpicyOption() {
        TableSession session = createSession(10L, true);
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(session));
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        MenuSpicyOption opt = new MenuSpicyOption();
        opt.setOptionName("순한맛");
        when(menuSpicyOptionRepository.findByMenuIdOrderByDisplayOrder(1L)).thenReturn(List.of(opt));

        CreateOrderRequest request = createOrderRequest(1L, 1, 9000, "극한맛", null);

        assertThatThrownBy(() -> service.createOrder(1L, 5L, 10L, request))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("INVALID_SPICY_OPTION"));
    }

    // === TC-CUST-013: 비활성 세션 → SESSION_NOT_FOUND ===
    @Test
    @DisplayName("TC-CUST-013: 비활성 세션 → SESSION_NOT_FOUND")
    void createOrder_sessionNotFound() {
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.empty());

        CreateOrderRequest request = createOrderRequest(1L, 1, 9000, null, null);

        assertThatThrownBy(() -> service.createOrder(1L, 5L, 10L, request))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("SESSION_NOT_FOUND"));
    }

    // === TC-CUST-014: 주문 번호 채번 — 낙관적 재시도 ===
    @Test
    @DisplayName("TC-CUST-014: 주문 번호 채번 — 기존 주문 있을 때 순번 증가")
    void createOrder_orderNumberIncrement() {
        TableSession session = createSession(10L, true);
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(session));
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(orderRepository.countByStoreIdAndOrderNumberStartingWith(eq(1L), anyString())).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(44L);
            return o;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = createOrderRequest(1L, 1, 9000, null, null);
        OrderResponse result = service.createOrder(1L, 5L, 10L, request);

        assertThat(result.getOrderNumber()).endsWith("-002");
    }

    // === TC-CUST-015: SSE 발행 실패해도 주문 성공 ===
    @Test
    @DisplayName("TC-CUST-015: SSE 발행 실패해도 주문 성공")
    void createOrder_sseFailureIgnored() {
        TableSession session = createSession(10L, true);
        when(tableSessionRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(session));
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(orderRepository.countByStoreIdAndOrderNumberStartingWith(eq(1L), anyString())).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(45L);
            return o;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SSE failure")).when(sseService).publishNewOrder(anyLong(), any());

        CreateOrderRequest request = createOrderRequest(1L, 1, 9000, null, null);
        OrderResponse result = service.createOrder(1L, 5L, 10L, request);

        assertThat(result.getOrderId()).isEqualTo(45L);
    }

    // === TC-CUST-016: 주문 내역 페이지네이션 조회 ===
    @Test
    @DisplayName("TC-CUST-016: 세션의 주문 내역 페이지네이션 조회")
    void getOrders_returnsPaginated() {
        Order o1 = createOrder(42L, 10L, "20260306-001", 18000);
        Order o2 = createOrder(43L, 10L, "20260306-002", 8000);
        Page<Order> page = new PageImpl<>(List.of(o1, o2), PageRequest.of(0, 10), 2);
        when(orderRepository.findBySessionIdOrderByCreatedAtDesc(eq(10L), any())).thenReturn(page);

        OrderItem item1 = createOrderItem(1L, 42L, "김치찌개", 2, 9000);
        OrderItem item2 = createOrderItem(2L, 43L, "된장찌개", 1, 8000);
        when(orderItemRepository.findByOrderIdIn(List.of(42L, 43L))).thenReturn(List.of(item1, item2));

        OrderListResponse result = service.getOrders(10L, 0, 10);

        assertThat(result.getOrders()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    // === TC-CUST-017: sessionId null이면 빈 목록 ===
    @Test
    @DisplayName("TC-CUST-017: sessionId가 null이면 빈 목록 반환")
    void getOrders_nullSession_returnsEmpty() {
        OrderListResponse result = service.getOrders(null, 0, 10);

        assertThat(result.getOrders()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // === Helper Methods ===
    private Menu createMenu(Long id, Long storeId, String name, int price) {
        Menu m = new Menu();
        m.setId(id);
        m.setStoreId(storeId);
        m.setName(name);
        m.setPrice(price);
        return m;
    }

    private TableSession createSession(Long id, boolean active) {
        TableSession s = new TableSession();
        s.setId(id);
        s.setActive(active);
        return s;
    }

    private Order createOrder(Long id, Long sessionId, String orderNumber, int totalAmount) {
        Order o = new Order();
        o.setId(id);
        o.setSessionId(sessionId);
        o.setOrderNumber(orderNumber);
        o.setTotalAmount(totalAmount);
        o.setStatus("WAITING");
        o.setCreatedAt(LocalDateTime.now());
        return o;
    }

    private OrderItem createOrderItem(Long id, Long orderId, String menuName, int qty, int unitPrice) {
        OrderItem i = new OrderItem();
        i.setId(id);
        i.setOrderId(orderId);
        i.setMenuName(menuName);
        i.setQuantity(qty);
        i.setUnitPrice(unitPrice);
        return i;
    }

    private CreateOrderRequest createOrderRequest(Long menuId, int qty, int unitPrice, String spicyOption, String specialRequest) {
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuId(menuId);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setSpicyOption(spicyOption);
        item.setSpecialRequest(specialRequest);
        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));
        return req;
    }
}
