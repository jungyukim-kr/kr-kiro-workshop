# Unit 5: Admin Backend - Domain Entities

## Unit 5에서 사용하는 엔티티

### 읽기/쓰기

| 엔티티 | 읽기 | 쓰기 | 용도 |
|--------|------|------|------|
| Order | 주문 목록 조회 | 상태 변경, 삭제 | 주문 모니터링/관리 |
| OrderItem | 주문 항목 조회 | 삭제 (주문 삭제 시) | 주문 상세 |
| TableSession | 활성 세션 조회 | 세션 종료 (active=false) | 세션 관리 |
| OrderHistory | 과거 이력 조회 | 세션 종료 시 생성 | 이력 보존 |
| OrderHistoryItem | 과거 이력 항목 조회 | 세션 종료 시 생성 | 이력 상세 |
| Menu | 메뉴 조회 | 생성/수정/삭제 | 메뉴 CRUD |
| StoreTable | 테이블 목록 조회 | PIN 변경 | 테이블 관리 |

### 읽기 전용

| 엔티티 | 용도 |
|--------|------|
| Store | 매장 정보 (JWT storeId 기반) |

### 사용하지 않음

| 엔티티 | 이유 |
|--------|------|
| Admin | Unit 2 인증에서 처리 |
| MenuSpicyOption | Q6에서 맵기 옵션 관리 스킵 (Could) |

---

## Repository 정의

### AdminOrderRepository
```
findByStoreIdAndSessionIdOrderByCreatedAtDesc(storeId, sessionId) → List<Order>
findByStoreIdAndStatusNotOrderByCreatedAtDesc(storeId, status) → List<Order>
deleteBySessionId(sessionId) → void
```

### AdminOrderItemRepository
```
findByOrderIdIn(orderIds) → List<OrderItem>
deleteByOrderId(orderId) → void
deleteByOrderIdIn(orderIds) → void
```

### AdminTableSessionRepository
```
findByStoreIdAndActiveTrue(storeId) → List<TableSession>
findByIdAndActiveTrue(id) → Optional<TableSession>
findByTableIdAndActiveTrue(tableId) → Optional<TableSession>
```

### OrderHistoryRepository
```
findByTableIdOrderByEndedAtDesc(tableId, Pageable) → Page<OrderHistory>
```

### OrderHistoryItemRepository
```
findByOrderHistoryIdIn(historyIds) → List<OrderHistoryItem>
```

### AdminMenuRepository
```
findByStoreIdOrderByDisplayOrder(storeId) → List<Menu>
findByIdAndStoreId(menuId, storeId) → Optional<Menu>
```

### AdminStoreTableRepository
```
findByStoreIdOrderByTableNumber(storeId) → List<StoreTable>
findByIdAndStoreId(tableId, storeId) → Optional<StoreTable>
```
