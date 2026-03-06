# Unit 5: Admin Backend - Business Logic Model

## API 엔드포인트 설계

| # | Method | Endpoint | 설명 | Story |
|---|--------|----------|------|-------|
| 1 | GET | `/api/stores/{storeId}/admin/dashboard` | 대시보드 전체 현황 | US-A02 |
| 2 | PUT | `/api/stores/{storeId}/admin/orders/{orderId}/status` | 주문 상태 변경 | US-A03 |
| 3 | DELETE | `/api/stores/{storeId}/admin/orders/{orderId}` | 주문 삭제 | US-A06 |
| 4 | PUT | `/api/stores/{storeId}/admin/tables/{tableId}/pin` | 테이블 PIN 변경 | US-A05 |
| 5 | POST | `/api/stores/{storeId}/admin/tables/{tableId}/close-session` | 세션 종료 (이용 완료) | US-A08 |
| 6 | GET | `/api/stores/{storeId}/admin/tables/{tableId}/history` | 과거 이력 조회 | US-A09 |
| 7 | GET | `/api/stores/{storeId}/admin/menus` | 메뉴 목록 조회 | US-A10 |
| 8 | POST | `/api/stores/{storeId}/admin/menus` | 메뉴 등록 | US-A10 |
| 9 | PUT | `/api/stores/{storeId}/admin/menus/{menuId}` | 메뉴 수정 | US-A10 |
| 10 | DELETE | `/api/stores/{storeId}/admin/menus/{menuId}` | 메뉴 삭제 | US-A10 |
| 11 | GET | `/api/stores/{storeId}/events` | SSE 구독 | US-A02 |

---

## Flow 1: 대시보드 전체 현황 (GET /admin/dashboard)

```
Request → JWT에서 storeId 확인
  → StoreTable 목록 조회 (tableNumber 순)
  → 각 테이블의 활성 세션 조회
  → 활성 세션이 있으면 해당 세션의 주문 목록 + 항목 조회
  → 테이블별 카드 데이터 조합하여 반환
```

### Response
```json
{
  "tables": [
    {
      "tableId": 1,
      "tableNumber": 1,
      "session": {
        "sessionId": 10,
        "startedAt": "2026-03-06T14:00:00+09:00",
        "totalAmount": 31000,
        "orders": [
          {
            "orderId": 42,
            "orderNumber": "20260306-001",
            "totalAmount": 18000,
            "status": "WAITING",
            "createdAt": "2026-03-06T15:30:00+09:00",
            "items": [
              {"menuName": "김치찌개", "quantity": 2, "unitPrice": 9000, "spicyOption": "보통", "specialRequest": "청양고추 빼주세요"}
            ]
          }
        ]
      }
    },
    {
      "tableId": 2,
      "tableNumber": 2,
      "session": null
    }
  ]
}
```

---

## Flow 2: 주문 상태 변경 (PUT /admin/orders/{orderId}/status)

### Request Body
```json
{
  "status": "PREPARING"
}
```

### 처리 플로우
```
1. orderId로 Order 조회 → 없으면 ORDER_NOT_FOUND
2. Order.storeId == JWT.storeId 확인
3. 상태 전이 검증 (단방향):
   WAITING → PREPARING → DONE
   그 외 전이 → INVALID_STATUS_TRANSITION
4. Order.status 업데이트
5. SSE: ORDER_STATUS_CHANGED 이벤트 발행
6. 응답 반환
```

### Response (200 OK)
```json
{
  "orderId": 42,
  "orderNumber": "20260306-001",
  "status": "PREPARING",
  "previousStatus": "WAITING"
}
```

---

## Flow 3: 주문 삭제 (DELETE /admin/orders/{orderId})

### 처리 플로우
```
1. orderId로 Order 조회 → 없으면 ORDER_NOT_FOUND
2. Order.storeId == JWT.storeId 확인
3. OrderItem 삭제 (Hard Delete)
4. Order 삭제 (Hard Delete)
5. SSE: ORDER_DELETED 이벤트 발행
6. 204 No Content 반환
```

---

## Flow 4: 테이블 PIN 변경 (PUT /admin/tables/{tableId}/pin)

### Request Body
```json
{
  "newPin": "5678"
}
```

### 처리 플로우
```
1. tableId로 StoreTable 조회 → 없으면 TABLE_NOT_FOUND
2. StoreTable.storeId == JWT.storeId 확인
3. newPin bcrypt 해싱
4. StoreTable.passwordHash 업데이트
5. 응답 반환
```

### Response (200 OK)
```json
{
  "tableId": 1,
  "tableNumber": 1,
  "message": "PIN이 변경되었습니다"
}
```

---

## Flow 5: 세션 종료 — 이용 완료 (POST /admin/tables/{tableId}/close-session)

### 처리 플로우
```
1. tableId로 활성 세션 조회 → 없으면 SESSION_NOT_FOUND
2. 세션의 모든 주문 + 주문 항목 조회

3. OrderHistory 생성:
   → storeId, tableId, sessionId, sessionCode
   → totalAmount = Σ(주문별 totalAmount)
   → orderCount = 주문 수
   → startedAt = session.startedAt
   → endedAt = now()

4. OrderHistoryItem 생성 (각 주문 항목):
   → orderHistoryId, orderNumber, menuName, quantity, unitPrice
   → spicyOption, specialRequest, orderedAt, orderStatus

5. 원본 OrderItem 전체 삭제
6. 원본 Order 전체 삭제
7. TableSession.active = false, endedAt = now()

8. SSE: SESSION_CLOSED 이벤트 발행
9. 응답 반환
```

### Response (200 OK)
```json
{
  "tableId": 1,
  "tableNumber": 1,
  "sessionId": 10,
  "totalAmount": 31000,
  "orderCount": 3,
  "endedAt": "2026-03-06T18:00:00+09:00"
}
```

---

## Flow 6: 과거 이력 조회 (GET /admin/tables/{tableId}/history?page=0&size=10)

### 처리 플로우
```
1. tableId + storeId 확인
2. OrderHistory를 endedAt 내림차순 페이지네이션 조회
3. 각 OrderHistory의 OrderHistoryItem 조회
4. 페이지 정보와 함께 반환
```

### Response
```json
{
  "histories": [
    {
      "historyId": 1,
      "sessionCode": "abc-123",
      "totalAmount": 31000,
      "orderCount": 3,
      "startedAt": "2026-03-06T14:00:00+09:00",
      "endedAt": "2026-03-06T18:00:00+09:00",
      "items": [
        {"orderNumber": "20260306-001", "menuName": "김치찌개", "quantity": 2, "unitPrice": 9000, "spicyOption": "보통", "specialRequest": null, "orderedAt": "2026-03-06T15:30:00+09:00", "orderStatus": "DONE"}
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 5,
  "totalPages": 1
}
```

---

## Flow 7-10: 메뉴 CRUD

### GET /admin/menus → 메뉴 목록 조회
```json
{
  "menus": [
    {"id": 1, "name": "김치찌개", "price": 9000, "description": "...", "category": "찌개", "imageUrl": "...", "spicyLevel": "매움", "displayOrder": 1}
  ]
}
```

### POST /admin/menus → 메뉴 등록
```json
// Request
{"name": "새메뉴", "price": 10000, "description": "설명", "category": "찌개", "imageUrl": null, "spicyLevel": null, "displayOrder": 0}
// Response (201 Created)
{"id": 11, "name": "새메뉴", "price": 10000, ...}
```

### PUT /admin/menus/{menuId} → 메뉴 수정
```json
// Request
{"name": "수정메뉴", "price": 11000, ...}
// Response (200 OK)
{"id": 1, "name": "수정메뉴", "price": 11000, ...}
```
- 가격 변경 시 SSE: `MENU_UPDATED` 이벤트 발행

### DELETE /admin/menus/{menuId} → 메뉴 삭제
- 204 No Content
- SSE: `MENU_UPDATED` 이벤트 발행

---

## Flow 11: SSE 구독 (GET /events)

### 처리 플로우
```
1. JWT에서 storeId, role=ADMIN 확인
2. SseEmitter 생성 (timeout: 30분)
3. storeId별 emitter 목록에 등록
4. 연결 유지 (heartbeat: 30초)
5. 이벤트 발생 시 해당 storeId의 모든 emitter에 전송
```

### SSE 이벤트 정의

| 이벤트 | 발행 시점 | 데이터 |
|--------|-----------|--------|
| `NEW_ORDER` | 고객 주문 생성 (Unit 3) | {orderId, orderNumber, tableId, totalAmount, items} |
| `ORDER_STATUS_CHANGED` | 관리자 상태 변경 | {orderId, orderNumber, status, previousStatus} |
| `ORDER_DELETED` | 관리자 주문 삭제 | {orderId, orderNumber, tableId} |
| `SESSION_CLOSED` | 관리자 세션 종료 | {tableId, sessionId, totalAmount} |
| `MENU_UPDATED` | 메뉴 변경/삭제 | {action: "UPDATED"/"DELETED", menuId} |
