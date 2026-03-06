# Unit 3: Customer Backend - Domain Entities

## Unit 3에서 사용하는 엔티티

Unit 1에서 정의된 엔티티를 참조하며, Customer BE 관점의 사용 방식을 정의합니다.

### 읽기 전용 (조회만)

| 엔티티 | 용도 |
|--------|------|
| Menu | 메뉴 목록/상세 조회 |
| MenuSpicyOption | 메뉴별 맵기 옵션 조회 |
| Store | 매장 정보 (JWT storeId 기반) |
| StoreTable | 테이블 정보 (JWT tableId 기반) |

### 읽기/쓰기

| 엔티티 | 읽기 | 쓰기 | 용도 |
|--------|------|------|------|
| TableSession | 활성 세션 조회 | 세션 생성 (첫 주문 시) | 세션 관리 |
| Order | 주문 내역 조회 | 주문 생성 | 주문 관리 |
| OrderItem | 주문 항목 조회 | 주문 항목 생성 | 주문 상세 |

### 사용하지 않음

| 엔티티 | 이유 |
|--------|------|
| Admin | 관리자 전용 |
| OrderHistory | 관리자가 세션 종료 시 생성 (Unit 5) |
| OrderHistoryItem | 관리자 전용 (Unit 5) |

---

## Repository 정의

### MenuRepository
```
findByStoreIdOrderByDisplayOrder(storeId) → List<Menu>
findDistinctCategoryByStoreId(storeId) → List<String>
findById(menuId) → Optional<Menu>
```

### MenuSpicyOptionRepository
```
findByMenuIdOrderByDisplayOrder(menuId) → List<MenuSpicyOption>
```

### TableSessionRepository
```
findByTableIdAndActiveTrue(tableId) → Optional<TableSession>
save(tableSession) → TableSession
```

### OrderRepository
```
findBySessionIdOrderByCreatedAtDesc(sessionId) → List<Order>
findBySessionIdOrderByCreatedAtDesc(sessionId, Pageable) → Page<Order>
countByStoreIdAndOrderNumberStartingWith(storeId, datePrefix) → Long
```

### OrderItemRepository
```
findByOrderId(orderId) → List<OrderItem>
saveAll(orderItems) → List<OrderItem>
```
