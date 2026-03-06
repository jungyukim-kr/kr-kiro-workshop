# Test Plan for Unit 3: Customer Backend

## Unit Overview
- **Unit**: unit-3-customer-be
- **Stories**: US-C02, US-C05, US-C06, US-C07, US-C08
- **Approach**: Unit tests with Mockito (Service layer), MockMvc (Controller layer)

---

## Service Layer Tests

### CustomerMenuService

#### getCategories()
- **TC-CUST-001**: 매장에 메뉴가 있을 때 카테고리 목록 반환
  - Given: storeId=1 매장에 "찌개", "볶음" 카테고리 메뉴 존재
  - When: getCategories(1) 호출
  - Then: ["볶음", "찌개"] 반환 (가나다순)
  - Story: US-C02
  - Status: ⬜ Not Started

- **TC-CUST-002**: 매장에 메뉴가 없을 때 빈 목록 반환
  - Given: storeId=99 매장에 메뉴 없음
  - When: getCategories(99) 호출
  - Then: 빈 배열 반환
  - Story: US-C02
  - Status: ⬜ Not Started

#### getMenusByCategory()
- **TC-CUST-003**: 카테고리별 메뉴 목록 반환 (hasSpicyOptions 포함)
  - Given: storeId=1, category="찌개", 김치찌개(맵기옵션 있음), 된장찌개(맵기옵션 없음)
  - When: getMenusByCategory(1, "찌개") 호출
  - Then: 2개 메뉴 반환, 김치찌개.hasSpicyOptions=true, 된장찌개.hasSpicyOptions=false
  - Story: US-C02
  - Status: ⬜ Not Started

- **TC-CUST-004**: 해당 카테고리에 메뉴 없으면 빈 목록 반환
  - Given: storeId=1, category="디저트" (메뉴 없음)
  - When: getMenusByCategory(1, "디저트") 호출
  - Then: 빈 배열 반환
  - Story: US-C02
  - Status: ⬜ Not Started

#### getSpicyOptions()
- **TC-CUST-005**: 메뉴의 맵기 옵션 목록 반환
  - Given: menuId=1 메뉴에 4개 맵기 옵션 존재
  - When: getSpicyOptions(1, 1) 호출
  - Then: 4개 옵션 반환 (displayOrder 순)
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-006**: 존재하지 않는 메뉴 → MENU_NOT_FOUND
  - Given: menuId=999 메뉴 없음
  - When: getSpicyOptions(1, 999) 호출
  - Then: CustomerException(MENU_NOT_FOUND) 발생
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-007**: 다른 매장의 메뉴 → MENU_NOT_FOUND
  - Given: menuId=1 메뉴의 storeId=2, 요청 storeId=1
  - When: getSpicyOptions(1, 1) 호출
  - Then: CustomerException(MENU_NOT_FOUND) 발생
  - Story: US-C05
  - Status: ⬜ Not Started

### CustomerOrderService

#### createOrder()
- **TC-CUST-008**: 정상 주문 생성 (세션 존재)
  - Given: sessionId=10 활성 세션, menuId=1 가격 9000원
  - When: createOrder(1, 5, 10, {items: [{menuId:1, qty:2, unitPrice:9000}]})
  - Then: Order 생성, orderNumber 형식 yyyyMMdd-NNN, totalAmount=18000, status=WAITING
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-009**: 세션 자동 생성 (sessionId == null)
  - Given: sessionId=null
  - When: createOrder(1, 5, null, {items: [...]})
  - Then: 새 TableSession 생성, Order에 새 sessionId 설정
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-010**: 메뉴 미존재 → MENU_NOT_FOUND
  - Given: menuId=999 메뉴 없음
  - When: createOrder(1, 5, 10, {items: [{menuId:999, ...}]})
  - Then: CustomerException(MENU_NOT_FOUND) 발생
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-011**: 가격 불일치 → PRICE_MISMATCH
  - Given: menuId=1 DB 가격 9000원, 요청 unitPrice=8000
  - When: createOrder(1, 5, 10, {items: [{menuId:1, unitPrice:8000, ...}]})
  - Then: CustomerException(PRICE_MISMATCH) 발생
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-012**: 잘못된 맵기 옵션 → INVALID_SPICY_OPTION
  - Given: menuId=1에 "순한맛","보통","매운맛" 옵션만 등록
  - When: createOrder(1, 5, 10, {items: [{menuId:1, spicyOption:"극한맛", ...}]})
  - Then: CustomerException(INVALID_SPICY_OPTION) 발생
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-013**: 비활성 세션 → SESSION_NOT_FOUND
  - Given: sessionId=10 세션 active=false
  - When: createOrder(1, 5, 10, {items: [...]})
  - Then: CustomerException(SESSION_NOT_FOUND) 발생
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-014**: 주문 번호 채번 — 낙관적 재시도
  - Given: 당일 해당 매장 주문 1건 존재
  - When: createOrder 호출
  - Then: orderNumber = "yyyyMMdd-002"
  - Story: US-C05
  - Status: ⬜ Not Started

- **TC-CUST-015**: SSE 발행 실패해도 주문 성공
  - Given: sseService.publishNewOrder() 예외 발생
  - When: createOrder 호출
  - Then: 주문 정상 생성, 예외 무시
  - Story: US-C05
  - Status: ⬜ Not Started

#### getOrders()
- **TC-CUST-016**: 세션의 주문 내역 페이지네이션 조회
  - Given: sessionId=10에 주문 2건 존재
  - When: getOrders(10, 0, 10) 호출
  - Then: 2건 반환, totalElements=2, totalPages=1
  - Story: US-C06, US-C07
  - Status: ⬜ Not Started

- **TC-CUST-017**: sessionId가 null이면 빈 목록 반환
  - Given: sessionId=null
  - When: getOrders(null, 0, 10) 호출
  - Then: 빈 목록, totalElements=0
  - Story: US-C08
  - Status: ⬜ Not Started

---

## Controller Layer Tests

### CustomerMenuController

- **TC-CUST-018**: GET /categories → 200 OK
  - Given: 카테고리 2개 존재
  - When: GET /api/stores/1/customer/categories
  - Then: 200, {"categories": ["볶음", "찌개"]}
  - Status: ⬜ Not Started

- **TC-CUST-019**: GET /menus?category=찌개 → 200 OK
  - Given: 찌개 카테고리 메뉴 2개
  - When: GET /api/stores/1/customer/menus?category=찌개
  - Then: 200, menus 배열 2개
  - Status: ⬜ Not Started

- **TC-CUST-020**: GET /menus/{menuId}/spicy-options → 200 OK
  - Given: menuId=1에 옵션 4개
  - When: GET /api/stores/1/customer/menus/1/spicy-options
  - Then: 200, options 배열 4개
  - Status: ⬜ Not Started

- **TC-CUST-021**: GET /menus/{menuId}/spicy-options → 404 MENU_NOT_FOUND
  - Given: menuId=999 없음
  - When: GET /api/stores/1/customer/menus/999/spicy-options
  - Then: 404, {"error": "MENU_NOT_FOUND"}
  - Status: ⬜ Not Started

### CustomerOrderController

- **TC-CUST-022**: POST /orders → 201 Created
  - Given: 유효한 주문 요청
  - When: POST /api/stores/1/customer/orders
  - Then: 201, orderId, orderNumber 포함
  - Status: ⬜ Not Started

- **TC-CUST-023**: POST /orders → 400 빈 items
  - Given: items 빈 배열
  - When: POST /api/stores/1/customer/orders
  - Then: 400, {"error": "VALIDATION_FAILED"}
  - Status: ⬜ Not Started

- **TC-CUST-024**: POST /orders → 409 PRICE_MISMATCH
  - Given: 가격 불일치
  - When: POST /api/stores/1/customer/orders
  - Then: 409, {"error": "PRICE_MISMATCH"}
  - Status: ⬜ Not Started

- **TC-CUST-025**: GET /orders → 200 OK 페이지네이션
  - Given: 주문 2건
  - When: GET /api/stores/1/customer/orders?page=0&size=10
  - Then: 200, orders 2건, totalElements=2
  - Status: ⬜ Not Started

---

## Requirements Coverage

| Story | Test Cases | Status |
|-------|-----------|--------|
| US-C02 | TC-CUST-001~004, TC-CUST-018~019 | ⬜ Pending |
| US-C05 | TC-CUST-005~015, TC-CUST-020~024 | ⬜ Pending |
| US-C06 | TC-CUST-016, TC-CUST-025 | ⬜ Pending |
| US-C07 | TC-CUST-016, TC-CUST-025 | ⬜ Pending |
| US-C08 | TC-CUST-017 | ⬜ Pending |
