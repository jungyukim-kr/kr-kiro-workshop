# Contract/Interface Definition for Unit 3: Customer Backend

## Unit Context
- **Stories**: US-C02 (메뉴 조회), US-C05 (주문하기), US-C06/C07/C08 (주문 내역 조회)
- **Dependencies**: Unit 1 (엔티티), Unit 2 (인증/보안 필터), Unit 5 (SSE — 인터페이스만 정의)
- **Database Entities**: Menu, MenuSpicyOption, TableSession, Order, OrderItem (읽기/쓰기)

---

## Exception Layer

### CustomerException (RuntimeException)
- `CustomerException(String errorCode, String message, HttpStatus status)`
- Fields: errorCode, message, httpStatus

### Error Codes
| Code | HttpStatus | 용도 |
|------|-----------|------|
| MENU_NOT_FOUND | 404 | 메뉴 미존재 |
| PRICE_MISMATCH | 409 | 가격 변경됨 |
| INVALID_SPICY_OPTION | 400 | 미등록 맵기 옵션 |
| SESSION_NOT_FOUND | 404 | 세션 비활성/미존재 |
| VALIDATION_FAILED | 400 | 입력 검증 실패 |

### CustomerExceptionHandler (@RestControllerAdvice)
- `handleCustomerException(CustomerException) -> ResponseEntity<ErrorResponse>`

---

## Repository Layer

### MenuRepository (JpaRepository<Menu, Long>)
- `findDistinctCategoryByStoreIdOrderByCategory(Long storeId) -> List<String>` — @Query JPQL
- `findByStoreIdAndCategoryOrderByDisplayOrder(Long storeId, String category) -> List<Menu>`

### MenuSpicyOptionRepository (JpaRepository<MenuSpicyOption, Long>)
- `findByMenuIdOrderByDisplayOrder(Long menuId) -> List<MenuSpicyOption>`
- `existsByMenuId(Long menuId) -> boolean`

### TableSessionRepository (JpaRepository<TableSession, Long>)
- `findByIdAndActiveTrue(Long id) -> Optional<TableSession>`
- Note: auth 패키지에 이미 존재하지만 customer 패키지에 별도 생성 (패키지 분리)

### OrderRepository (JpaRepository<Order, Long>)
- `findBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable) -> Page<Order>`
- `countByStoreIdAndOrderNumberStartingWith(Long storeId, String prefix) -> long`

### OrderItemRepository (JpaRepository<OrderItem, Long>)
- `findByOrderIdIn(List<Long> orderIds) -> List<OrderItem>`

---

## Service Layer

### CustomerMenuService
- `getCategories(Long storeId) -> CategoriesResponse`
  - Returns: 카테고리 문자열 목록
- `getMenusByCategory(Long storeId, String category) -> MenuListResponse`
  - Returns: 메뉴 DTO 목록 (hasSpicyOptions 포함)
- `getSpicyOptions(Long storeId, Long menuId) -> SpicyOptionsResponse`
  - Returns: 맵기 옵션 목록
  - Raises: CustomerException(MENU_NOT_FOUND)

### CustomerOrderService
- `createOrder(Long storeId, Long tableId, Long sessionId, CreateOrderRequest request) -> OrderResponse`
  - Args: storeId, tableId (JWT), sessionId (JWT, nullable), request body
  - Returns: 생성된 주문 정보
  - Raises: CustomerException(MENU_NOT_FOUND, PRICE_MISMATCH, INVALID_SPICY_OPTION, SESSION_NOT_FOUND)
- `getOrders(Long sessionId, int page, int size) -> OrderListResponse`
  - Args: sessionId (JWT, nullable), page, size
  - Returns: 주문 목록 + 페이지 정보

---

## DTO Layer

### Request
- `CreateOrderRequest` — items: List<OrderItemRequest>
- `OrderItemRequest` — menuId, quantity, unitPrice, spicyOption, specialRequest

### Response
- `CategoriesResponse` — categories: List<String>
- `MenuListResponse` — menus: List<MenuDto>
- `MenuDto` — id, name, price, description, category, imageUrl, spicyLevel, hasSpicyOptions
- `SpicyOptionsResponse` — options: List<SpicyOptionDto>
- `SpicyOptionDto` — id, optionName
- `OrderResponse` — orderId, orderNumber, totalAmount, status, createdAt, sessionId, items
- `OrderItemDto` — menuName, quantity, unitPrice, spicyOption, specialRequest
- `OrderListResponse` — orders, page, size, totalElements, totalPages

---

## Controller Layer (API)

### CustomerMenuController
- `GET /api/stores/{storeId}/customer/categories` → `getCategories(storeId)`
- `GET /api/stores/{storeId}/customer/menus?category=` → `getMenusByCategory(storeId, category)`
- `GET /api/stores/{storeId}/customer/menus/{menuId}/spicy-options` → `getSpicyOptions(storeId, menuId)`

### CustomerOrderController
- `POST /api/stores/{storeId}/customer/orders` → `createOrder(storeId, authInfo, request)`
- `GET /api/stores/{storeId}/customer/orders?page=&size=` → `getOrders(authInfo, page, size)`

---

## SSE Interface (Unit 5 연계)

### SseService (Interface — Unit 3에서 정의, Unit 5에서 구현)
- `publishNewOrder(Long storeId, OrderResponse order) -> void`
  - Fire-and-forget: 실패 시 로그만 남김

### NoOpSseService (Unit 3 기본 구현)
- Unit 5 구현 전까지 사용하는 빈 구현체
- `publishNewOrder(...)` → log.info만 출력
