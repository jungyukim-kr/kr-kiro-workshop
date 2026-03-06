# Application Design - Component Methods

> 상세 비즈니스 규칙은 Functional Design (CONSTRUCTION) 단계에서 정의됩니다.

## AuthComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `adminLogin(storeId, username, password)` | LoginRequest | JWT Token | 관리자 로그인 |
| `tableLogin(storeId, tableNumber, password)` | TableLoginRequest | JWT Token | 테이블 태블릿 인증 |
| `validateToken(token)` | String | AuthInfo | JWT 토큰 검증 |

## StoreComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `getStore(storeId)` | Long | StoreDTO | 매장 정보 조회 |

## MenuComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `getMenusByCategory(storeId, category)` | Long, String | List\<MenuDTO\> | 카테고리별 메뉴 조회 |
| `getCategories(storeId)` | Long | List\<String\> | 카테고리 목록 조회 |
| `createMenu(storeId, menuData)` | Long, MenuRequest | MenuDTO | 메뉴 등록 |
| `updateMenu(storeId, menuId, menuData)` | Long, Long, MenuRequest | MenuDTO | 메뉴 수정 |
| `deleteMenu(storeId, menuId)` | Long, Long | void | 메뉴 삭제 |
| `updateMenuOrder(storeId, orderList)` | Long, List\<MenuOrderRequest\> | void | 노출 순서 변경 |

## TableComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `setupTable(storeId, tableNumber, password)` | Long, Integer, String | TableDTO | 테이블 초기 설정 |
| `getTables(storeId)` | Long | List\<TableDTO\> | 테이블 목록 조회 |
| `startSession(storeId, tableId)` | Long, Long | SessionDTO | 세션 시작 (첫 주문 시 자동) |
| `endSession(storeId, tableId)` | Long, Long | void | 이용 완료 (세션 종료) |

## OrderComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `createOrder(storeId, tableId, orderData)` | Long, Long, OrderRequest | OrderDTO | 주문 생성 |
| `getOrdersBySession(storeId, tableId, sessionId)` | Long, Long, String | List\<OrderDTO\> | 현재 세션 주문 조회 |
| `getOrdersByStore(storeId)` | Long | List\<OrderDTO\> | 매장 전체 주문 조회 (관리자) |
| `updateOrderStatus(storeId, orderId, status)` | Long, Long, OrderStatus | OrderDTO | 주문 상태 변경 |
| `deleteOrder(storeId, orderId)` | Long, Long | void | 주문 삭제 (관리자) |

## OrderHistoryComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `archiveSessionOrders(storeId, tableId, sessionId)` | Long, Long, String | void | 세션 주문 이력 저장 |
| `getHistory(storeId, tableId, dateFrom, dateTo)` | Long, Long, Date, Date | List\<OrderHistoryDTO\> | 과거 주문 조회 |

## SSEComponent

| Method | Input | Output | 설명 |
|--------|-------|--------|------|
| `subscribe(storeId)` | Long | SseEmitter | SSE 연결 생성 |
| `publishNewOrder(storeId, order)` | Long, OrderDTO | void | 신규 주문 이벤트 |
| `publishStatusChange(storeId, order)` | Long, OrderDTO | void | 상태 변경 이벤트 |
