# Unit 5: 관리자 모듈 - API 규격서

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080/api/stores/{storeId}/admin` |
| Content-Type | `application/json` |
| 인증 방식 | Bearer Token (JWT, role=ADMIN) |
| 문자 인코딩 | UTF-8 |

---

## 공통 헤더

### 요청 헤더

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | O | `Bearer {JWT토큰}` (관리자 로그인 토큰) |

### 응답 헤더

| 헤더 | 조건 | 설명 |
|------|------|------|
| `X-New-Token` | 토큰 갱신 시 | 자동 갱신된 새 JWT 토큰 (Unit 2 JwtAuthenticationFilter에서 처리) |

---

## 공통 에러 응답 형식

```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 메시지",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### Unit 5 에러 코드

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 400 | `VALIDATION_FAILED` | 입력 검증 실패 |
| 400 | `INVALID_STATUS_TRANSITION` | 허용되지 않는 상태 전이 |
| 404 | `ORDER_NOT_FOUND` | 주문 미존재 |
| 404 | `TABLE_NOT_FOUND` | 테이블 미존재 |
| 404 | `SESSION_NOT_FOUND` | 활성 세션 미존재 |
| 404 | `MENU_NOT_FOUND` | 메뉴 미존재 |

### 공통 에러 코드 (Unit 2 인증 필터에서 처리)

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 401 | `TOKEN_MISSING` | Authorization 헤더 없음 |
| 401 | `TOKEN_EXPIRED` | JWT 토큰 만료 |
| 401 | `TOKEN_INVALID` | JWT 서명 불일치 또는 단일 세션 위반 |
| 403 | `STORE_ACCESS_DENIED` | URL의 storeId와 토큰의 storeId 불일치 |
| 403 | `ROLE_ACCESS_DENIED` | ADMIN 역할이 아닌 토큰으로 접근 |

---

## 접근 제어

모든 API는 Unit 2의 Security Filter Chain을 통과합니다:
- 역할: `ADMIN`만 접근 가능
- 매장 격리: URL의 `{storeId}`와 JWT의 `storeId`가 일치해야 함

---

## API 목록

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

## 1. 대시보드 전체 현황

매장의 전체 테이블 + 활성 세션 + 주문 현황을 한번에 조회합니다.

### 요청

```
GET /api/stores/{storeId}/admin/dashboard
Authorization: Bearer {JWT토큰}
```

### 응답 (200 OK)

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
              {
                "menuName": "김치찌개",
                "quantity": 2,
                "unitPrice": 9000,
                "spicyOption": "보통",
                "specialRequest": "청양고추 빼주세요"
              }
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

| 필드 | 타입 | 설명 |
|------|------|------|
| tables | Array | 테이블 목록 (tableNumber 오름차순) |
| tables[].tableId | Long | 테이블 ID |
| tables[].tableNumber | Integer | 테이블 번호 |
| tables[].session | Object \| null | 활성 세션 (없으면 null) |
| tables[].session.sessionId | Long | 세션 ID |
| tables[].session.startedAt | String | 세션 시작 시각 (ISO 8601) |
| tables[].session.totalAmount | Integer | 세션 총 주문액 |
| tables[].session.orders | Array | 주문 목록 (createdAt 내림차순) |
| tables[].session.orders[].orderId | Long | 주문 ID |
| tables[].session.orders[].orderNumber | String | 주문 번호 |
| tables[].session.orders[].totalAmount | Integer | 주문 금액 |
| tables[].session.orders[].status | String | 주문 상태 (WAITING/PREPARING/DONE) |
| tables[].session.orders[].createdAt | String | 주문 시각 (ISO 8601) |
| tables[].session.orders[].items | Array | 주문 항목 |
| tables[].session.orders[].items[].menuName | String | 메뉴명 |
| tables[].session.orders[].items[].quantity | Integer | 수량 |
| tables[].session.orders[].items[].unitPrice | Integer | 단가 |
| tables[].session.orders[].items[].spicyOption | String \| null | 맵기 옵션 |
| tables[].session.orders[].items[].specialRequest | String \| null | 요청사항 |

### 비즈니스 로직

1. JWT에서 `storeId` 확인
2. 해당 매장의 전체 테이블 조회 (tableNumber 오름차순)
3. 각 테이블의 활성 세션 조회
4. 활성 세션이 있으면 해당 세션의 주문 + 항목 조회
5. session.totalAmount = Σ(주문별 totalAmount)

---

## 2. 주문 상태 변경

주문의 상태를 다음 단계로 변경합니다. 단방향만 허용됩니다.

### 요청

```
PUT /api/stores/{storeId}/admin/orders/{orderId}/status
Content-Type: application/json
Authorization: Bearer {JWT토큰}
```

#### Request Body

```json
{
  "status": "PREPARING"
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| status | String | O | WAITING, PREPARING, DONE 중 하나 | 변경할 상태 |

### 응답 (200 OK)

```json
{
  "orderId": 42,
  "orderNumber": "20260306-001",
  "status": "PREPARING",
  "previousStatus": "WAITING"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Long | 주문 ID |
| orderNumber | String | 주문 번호 |
| status | String | 변경된 상태 |
| previousStatus | String | 이전 상태 |

#### 주문 미존재 (404)

```json
{
  "error": "ORDER_NOT_FOUND",
  "message": "존재하지 않는 주문입니다 (orderId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

#### 잘못된 상태 전이 (400)

```json
{
  "error": "INVALID_STATUS_TRANSITION",
  "message": "허용되지 않는 상태 변경입니다 (DONE → WAITING)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 상태 전이 규칙

| 현재 → 변경 | 허용 |
|-------------|------|
| WAITING → PREPARING | ✓ |
| PREPARING → DONE | ✓ |
| 그 외 모든 전이 | ✗ |

### 비즈니스 로직

1. orderId로 Order 조회 → 없으면 ORDER_NOT_FOUND
2. Order.storeId == JWT.storeId 확인
3. 상태 전이 검증 → 불허 시 INVALID_STATUS_TRANSITION
4. Order.status 업데이트
5. SSE: `ORDER_STATUS_CHANGED` 이벤트 발행

---

## 3. 주문 삭제

주문을 DB에서 물리 삭제합니다.

### 요청

```
DELETE /api/stores/{storeId}/admin/orders/{orderId}
Authorization: Bearer {JWT토큰}
```

### 응답 (204 No Content)

> Response Body 없음

#### 주문 미존재 (404)

```json
{
  "error": "ORDER_NOT_FOUND",
  "message": "존재하지 않는 주문입니다 (orderId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

1. orderId로 Order 조회 → 없으면 ORDER_NOT_FOUND
2. Order.storeId == JWT.storeId 확인
3. OrderItem 삭제 (FK: orderId)
4. Order 삭제
5. SSE: `ORDER_DELETED` 이벤트 발행 (tableId 포함)


---

## 4. 테이블 PIN 변경

테이블 태블릿의 로그인 PIN을 변경합니다.

### 요청

```
PUT /api/stores/{storeId}/admin/tables/{tableId}/pin
Content-Type: application/json
Authorization: Bearer {JWT토큰}
```

#### Request Body

```json
{
  "newPin": "5678"
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| newPin | String | O | 정확히 4자리 숫자 (`^\d{4}$`) | 새 PIN |

### 응답 (200 OK)

```json
{
  "tableId": 1,
  "tableNumber": 1,
  "message": "PIN이 변경되었습니다"
}
```

#### 테이블 미존재 (404)

```json
{
  "error": "TABLE_NOT_FOUND",
  "message": "존재하지 않는 테이블입니다 (tableId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

#### 입력 검증 실패 (400)

```json
{
  "error": "VALIDATION_FAILED",
  "message": "4자리 숫자 PIN을 입력해주세요",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

1. tableId로 StoreTable 조회 → 없으면 TABLE_NOT_FOUND
2. StoreTable.storeId == JWT.storeId 확인
3. newPin을 bcrypt로 해싱
4. StoreTable.passwordHash 업데이트

---

## 5. 세션 종료 (이용 완료)

테이블의 활성 세션을 종료하고, 주문 내역을 OrderHistory로 스냅샷 저장합니다.

### 요청

```
POST /api/stores/{storeId}/admin/tables/{tableId}/close-session
Authorization: Bearer {JWT토큰}
```

> Request Body 없음

### 응답 (200 OK)

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

| 필드 | 타입 | 설명 |
|------|------|------|
| tableId | Long | 테이블 ID |
| tableNumber | Integer | 테이블 번호 |
| sessionId | Long | 종료된 세션 ID |
| totalAmount | Integer | 세션 총 주문액 |
| orderCount | Integer | 총 주문 수 |
| endedAt | String | 종료 시각 (ISO 8601) |

#### 활성 세션 없음 (404)

```json
{
  "error": "SESSION_NOT_FOUND",
  "message": "활성 세션이 없습니다",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

```
1. tableId로 활성 세션 조회 → 없으면 SESSION_NOT_FOUND
2. 세션의 모든 주문 + 주문 항목 조회

3. OrderHistory 생성:
   → storeId, tableId, sessionId, sessionCode
   → totalAmount = Σ(주문별 totalAmount)
   → orderCount = 주문 수
   → startedAt = session.startedAt, endedAt = now()

4. OrderHistoryItem 생성 (각 주문의 각 항목):
   → orderHistoryId, orderNumber, menuName, quantity, unitPrice
   → spicyOption, specialRequest, orderedAt, orderStatus

5. 원본 OrderItem 전체 삭제
6. 원본 Order 전체 삭제
7. TableSession.active = false, endedAt = now()

8. SSE: SESSION_CLOSED 이벤트 발행
```

### 트랜잭션 범위

- 단계 2~7: 단일 `@Transactional` — 실패 시 전체 롤백
- 단계 8: 트랜잭션 커밋 후 별도 실행

---

## 6. 과거 이력 조회

특정 테이블의 과거 세션 이력을 페이지네이션으로 조회합니다.

### 요청

```
GET /api/stores/{storeId}/admin/tables/{tableId}/history?page={page}&size={size}
Authorization: Bearer {JWT토큰}
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | Integer | X | 0 | 페이지 번호 (0부터) |
| size | Integer | X | 10 | 페이지 크기 |

### 응답 (200 OK)

```json
{
  "histories": [
    {
      "historyId": 1,
      "sessionCode": "abc-123-def",
      "totalAmount": 31000,
      "orderCount": 3,
      "startedAt": "2026-03-06T14:00:00+09:00",
      "endedAt": "2026-03-06T18:00:00+09:00",
      "items": [
        {
          "orderNumber": "20260306-001",
          "menuName": "김치찌개",
          "quantity": 2,
          "unitPrice": 9000,
          "spicyOption": "보통",
          "specialRequest": null,
          "orderedAt": "2026-03-06T15:30:00+09:00",
          "orderStatus": "DONE"
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 5,
  "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| histories | Array | 세션 이력 목록 (endedAt 내림차순) |
| histories[].historyId | Long | 이력 ID |
| histories[].sessionCode | String | 세션 코드 |
| histories[].totalAmount | Integer | 세션 총 주문액 |
| histories[].orderCount | Integer | 총 주문 수 |
| histories[].startedAt | String | 세션 시작 시각 |
| histories[].endedAt | String | 세션 종료 시각 |
| histories[].items | Array | 주문 항목 목록 |
| histories[].items[].orderNumber | String | 주문 번호 |
| histories[].items[].menuName | String | 메뉴명 (스냅샷) |
| histories[].items[].quantity | Integer | 수량 |
| histories[].items[].unitPrice | Integer | 단가 (스냅샷) |
| histories[].items[].spicyOption | String \| null | 맵기 옵션 |
| histories[].items[].specialRequest | String \| null | 요청사항 |
| histories[].items[].orderedAt | String | 주문 시각 |
| histories[].items[].orderStatus | String | 주문 상태 (스냅샷) |
| page | Integer | 현재 페이지 |
| size | Integer | 페이지 크기 |
| totalElements | Long | 전체 이력 수 |
| totalPages | Integer | 전체 페이지 수 |

### 비즈니스 로직

1. tableId + storeId 확인
2. OrderHistory를 endedAt 내림차순 페이지네이션 조회
3. 각 OrderHistory의 OrderHistoryItem 조회
4. 페이지 정보와 함께 반환

---

## 7. 메뉴 목록 조회

매장의 전체 메뉴를 조회합니다.

### 요청

```
GET /api/stores/{storeId}/admin/menus
Authorization: Bearer {JWT토큰}
```

### 응답 (200 OK)

```json
{
  "menus": [
    {
      "id": 1,
      "name": "김치찌개",
      "price": 9000,
      "description": "돼지고기와 묵은지로 끓인 김치찌개",
      "category": "찌개",
      "imageUrl": "https://example.com/kimchi.jpg",
      "spicyLevel": "매움",
      "displayOrder": 1
    }
  ]
}
```

---

## 8. 메뉴 등록

새 메뉴를 등록합니다.

### 요청

```
POST /api/stores/{storeId}/admin/menus
Content-Type: application/json
Authorization: Bearer {JWT토큰}
```

#### Request Body

```json
{
  "name": "새메뉴",
  "price": 10000,
  "description": "메뉴 설명",
  "category": "찌개",
  "imageUrl": null,
  "spicyLevel": null,
  "displayOrder": 0
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| name | String | O | 1~100자 | 메뉴명 |
| price | Integer | O | 1 이상 | 가격 |
| description | String | X | - | 설명 |
| category | String | O | 1~50자 | 카테고리 |
| imageUrl | String | X | 최대 500자 | 이미지 URL |
| spicyLevel | String | X | 최대 20자 | 맵기 수준 |
| displayOrder | Integer | X | 기본 0 | 노출 순서 |

### 응답 (201 Created)

```json
{
  "id": 11,
  "name": "새메뉴",
  "price": 10000,
  "description": "메뉴 설명",
  "category": "찌개",
  "imageUrl": null,
  "spicyLevel": null,
  "displayOrder": 0
}
```

---

## 9. 메뉴 수정

기존 메뉴를 수정합니다. 가격 변경 시 SSE `MENU_UPDATED` 이벤트가 발행됩니다.

### 요청

```
PUT /api/stores/{storeId}/admin/menus/{menuId}
Content-Type: application/json
Authorization: Bearer {JWT토큰}
```

#### Request Body

```json
{
  "name": "수정메뉴",
  "price": 11000,
  "description": "수정된 설명",
  "category": "찌개",
  "imageUrl": null,
  "spicyLevel": "보통",
  "displayOrder": 1
}
```

### 응답 (200 OK)

```json
{
  "id": 1,
  "name": "수정메뉴",
  "price": 11000,
  "description": "수정된 설명",
  "category": "찌개",
  "imageUrl": null,
  "spicyLevel": "보통",
  "displayOrder": 1
}
```

#### 메뉴 미존재 (404)

```json
{
  "error": "MENU_NOT_FOUND",
  "message": "존재하지 않는 메뉴입니다 (menuId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

1. menuId + storeId로 메뉴 조회 → 없으면 MENU_NOT_FOUND
2. 메뉴 필드 업데이트
3. 가격 변경 시 SSE: `MENU_UPDATED` 이벤트 발행

---

## 10. 메뉴 삭제

메뉴를 삭제합니다. 해당 메뉴의 맵기 옵션(MenuSpicyOption)도 함께 삭제됩니다.

### 요청

```
DELETE /api/stores/{storeId}/admin/menus/{menuId}
Authorization: Bearer {JWT토큰}
```

### 응답 (204 No Content)

> Response Body 없음

#### 메뉴 미존재 (404)

```json
{
  "error": "MENU_NOT_FOUND",
  "message": "존재하지 않는 메뉴입니다 (menuId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

1. menuId + storeId로 메뉴 조회 → 없으면 MENU_NOT_FOUND
2. MenuSpicyOption 삭제 (FK: menuId)
3. Menu 삭제
4. SSE: `MENU_UPDATED` 이벤트 발행

---

## 11. SSE 구독

관리자 대시보드에서 실시간 이벤트를 수신하기 위한 SSE 연결을 생성합니다.

### 요청

```
GET /api/stores/{storeId}/events
Authorization: Bearer {JWT토큰}
Accept: text/event-stream
```

### 응답 (200 OK, Content-Type: text/event-stream)

연결 후 이벤트가 발생할 때마다 다음 형식으로 전송됩니다:

```
event: NEW_ORDER
data: {"orderId":42,"orderNumber":"20260306-001","tableId":1,"totalAmount":18000,"items":[...]}

event: ORDER_STATUS_CHANGED
data: {"orderId":42,"orderNumber":"20260306-001","status":"PREPARING","previousStatus":"WAITING"}

event: ORDER_DELETED
data: {"orderId":42,"orderNumber":"20260306-001","tableId":1}

event: SESSION_CLOSED
data: {"tableId":1,"sessionId":10,"totalAmount":31000}

event: MENU_UPDATED
data: {"action":"UPDATED","menuId":1}
```

### SSE 이벤트 상세

| 이벤트 | 발행 시점 | 데이터 필드 |
|--------|-----------|-------------|
| `NEW_ORDER` | 고객 주문 생성 (Unit 3) | orderId, orderNumber, tableId, totalAmount, items |
| `ORDER_STATUS_CHANGED` | 관리자 상태 변경 | orderId, orderNumber, status, previousStatus |
| `ORDER_DELETED` | 관리자 주문 삭제 | orderId, orderNumber, tableId |
| `SESSION_CLOSED` | 관리자 세션 종료 | tableId, sessionId, totalAmount |
| `MENU_UPDATED` | 메뉴 변경/삭제 | action (UPDATED/DELETED), menuId |

### 연결 관리

| 항목 | 값 | 설명 |
|------|-----|------|
| Timeout | 30분 | SseEmitter 타임아웃 |
| Heartbeat | 30초 | 빈 comment 전송 (연결 유지) |
| 재연결 | 클라이언트 | 연결 끊김 시 클라이언트에서 재연결 |
| 매장 격리 | storeId별 | 해당 매장 이벤트만 수신 |

### 비즈니스 로직

1. JWT에서 storeId, role=ADMIN 확인
2. SseEmitter 생성 (timeout: 30분)
3. storeId별 emitter 목록에 등록
4. onCompletion/onTimeout/onError 시 emitter 제거
5. 이벤트 발생 시 해당 storeId의 모든 emitter에 전송

### 안전망

- SSE 연결이 끊어질 수 있으므로, Unit 6 (Admin FE)에서 30초~1분 주기로 대시보드 API polling 권장
