package com.tableorder.customer.service;

import com.tableorder.common.entity.*;
import com.tableorder.common.service.SseService;
import com.tableorder.customer.dto.*;
import com.tableorder.customer.exception.CustomerException;
import com.tableorder.customer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerOrderService {
    private static final Logger log = LoggerFactory.getLogger(CustomerOrderService.class);
    private static final int MAX_RETRY = 3;

    private final MenuRepository menuRepository;
    private final MenuSpicyOptionRepository menuSpicyOptionRepository;
    private final CustomerTableSessionRepository tableSessionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SseService sseService;

    public CustomerOrderService(MenuRepository menuRepository,
                                MenuSpicyOptionRepository menuSpicyOptionRepository,
                                CustomerTableSessionRepository tableSessionRepository,
                                OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                SseService sseService) {
        this.menuRepository = menuRepository;
        this.menuSpicyOptionRepository = menuSpicyOptionRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.sseService = sseService;
    }

    @Transactional
    public OrderResponse createOrder(Long storeId, Long tableId, Long sessionId, CreateOrderRequest request) {
        // 1. 세션 처리
        Long resolvedSessionId;
        if (sessionId == null) {
            TableSession session = new TableSession();
            session.setStoreId(storeId);
            session.setTableId(tableId);
            session.setSessionCode(UUID.randomUUID().toString());
            session.setActive(true);
            session = tableSessionRepository.save(session);
            resolvedSessionId = session.getId();
        } else {
            tableSessionRepository.findByIdAndActiveTrue(sessionId)
                    .orElseThrow(() -> new CustomerException("SESSION_NOT_FOUND",
                            "유효하지 않은 세션입니다", HttpStatus.NOT_FOUND));
            resolvedSessionId = sessionId;
        }

        // 2. 주문 항목 검증
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;
        for (OrderItemRequest item : request.getItems()) {
            Menu menu = menuRepository.findById(item.getMenuId())
                    .filter(m -> m.getStoreId().equals(storeId))
                    .orElseThrow(() -> new CustomerException("MENU_NOT_FOUND",
                            "존재하지 않는 메뉴입니다 (menuId: " + item.getMenuId() + ")", HttpStatus.NOT_FOUND));

            if (item.getUnitPrice() != menu.getPrice()) {
                throw new CustomerException("PRICE_MISMATCH",
                        "메뉴 가격이 변경되었습니다. 장바구니를 확인해주세요", HttpStatus.CONFLICT);
            }

            if (item.getSpicyOption() != null) {
                List<MenuSpicyOption> options = menuSpicyOptionRepository.findByMenuIdOrderByDisplayOrder(menu.getId());
                boolean valid = options.stream().anyMatch(o -> o.getOptionName().equals(item.getSpicyOption()));
                if (!valid) {
                    throw new CustomerException("INVALID_SPICY_OPTION",
                            "유효하지 않은 맵기 옵션입니다 (" + menu.getName() + ": " + item.getSpicyOption() + ")",
                            HttpStatus.BAD_REQUEST);
                }
            }

            OrderItem oi = new OrderItem();
            oi.setMenuId(menu.getId());
            oi.setMenuName(menu.getName());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(item.getUnitPrice());
            oi.setSpicyOption(item.getSpicyOption());
            oi.setSpecialRequest(item.getSpecialRequest());
            orderItems.add(oi);
            totalAmount += item.getUnitPrice() * item.getQuantity();
        }

        // 3. 주문 번호 채번 + 주문 생성
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.countByStoreIdAndOrderNumberStartingWith(storeId, datePrefix);
        String orderNumber = datePrefix + "-" + String.format("%03d", count + 1);

        Order order = new Order();
        order.setStoreId(storeId);
        order.setTableId(tableId);
        order.setSessionId(resolvedSessionId);
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setStatus("WAITING");
        order = orderRepository.save(order);

        // 4. 주문 항목 저장
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
        }
        orderItemRepository.saveAll(orderItems);

        // 5. 응답 생성
        OrderResponse response = toOrderResponse(order, orderItems, resolvedSessionId);

        // 6. SSE 발행 (fire-and-forget)
        try {
            sseService.publishNewOrder(storeId, response);
        } catch (Exception e) {
            log.warn("SSE 발행 실패: {}", e.getMessage());
        }

        return response;
    }

    public OrderListResponse getOrders(Long sessionId, int page, int size) {
        if (sessionId == null) {
            OrderListResponse response = new OrderListResponse();
            response.setOrders(Collections.emptyList());
            response.setPage(page);
            response.setSize(size);
            response.setTotalElements(0);
            response.setTotalPages(0);
            return response;
        }

        Page<Order> orderPage = orderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, PageRequest.of(page, size));
        List<Long> orderIds = orderPage.getContent().stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderIds.isEmpty() ? Collections.emptyList() : orderItemRepository.findByOrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<OrderResponse> orders = orderPage.getContent().stream()
                .map(o -> toOrderResponse(o, itemsByOrderId.getOrDefault(o.getId(), Collections.emptyList()), null))
                .toList();

        OrderListResponse response = new OrderListResponse();
        response.setOrders(orders);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        return response;
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items, Long sessionId) {
        OrderResponse r = new OrderResponse();
        r.setOrderId(order.getId());
        r.setOrderNumber(order.getOrderNumber());
        r.setTotalAmount(order.getTotalAmount());
        r.setStatus(order.getStatus());
        r.setCreatedAt(order.getCreatedAt().atOffset(OffsetDateTime.now().getOffset())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        r.setSessionId(sessionId != null ? sessionId : order.getSessionId());
        r.setItems(items.stream().map(i -> {
            OrderItemDto dto = new OrderItemDto();
            dto.setMenuName(i.getMenuName());
            dto.setQuantity(i.getQuantity());
            dto.setUnitPrice(i.getUnitPrice());
            dto.setSpicyOption(i.getSpicyOption());
            dto.setSpecialRequest(i.getSpecialRequest());
            return dto;
        }).toList());
        return r;
    }
}
