# Unit 3: 고객 주문 모듈 - API 규격서

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080/api/stores/{storeId}/customer` |
| Content-Type | `application/json` |
| 인증 방식 | Bearer Token (JWT, role=TABLE) |
| 문자 인코딩 | UTF-8 |

---

## 공통 헤더

### 요청 헤더

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | O | `Bearer {JWT토큰}` (테이블 로그인 토큰) |

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

### Unit 3 에러 코드

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 400 | `VALIDATION_FAILED` | 입력 검증 실패 |
| 400 | `INVALID_SPICY_OPTION` | 미등록 맵기 옵션 |
| 404 | `MENU_NOT_FOUND` | 메뉴 미존재 |
| 404 | `SESSION_NOT_FOUND` | 세션 미존재 또는 비활성 |
| 409 | `PRICE_MISMATCH` | 메뉴 가격 변경됨 |

### 공통 에러 코드 (Unit 2 인증 필터에서 처리)

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 401 | `TOKEN_MISSING` | Authorization 헤더 없음 |
| 401 | `TOKEN_EXPIRED` | JWT 토큰 만료 |
| 401 | `TOKEN_INVALID` | JWT 서명 불일치 또는 단일 세션 위반 |
| 403 | `STORE_ACCESS_DENIED` | URL의 storeId와 토큰의 storeId 불일치 |
| 403 | `ROLE_ACCESS_DENIED` | TABLE 역할이 아닌 토큰으로 접근 |

---

## 접근 제어

모든 API는 Unit 2의 Security Filter Chain을 통과합니다:

```
HTTP Request
    │
    ▼
[1] JwtAuthenticationFilter ─── 토큰 검증, SecurityContext 설정
    │
    ▼
[2] StoreAccessFilter ─── URL storeId vs 토큰 storeId 일치 검증
    │
    ▼
[3] AuthorizationFilter ─── role=TABLE 확인
    │
    ▼
[4] CustomerController
```

- 역할: `TABLE`만 접근 가능
- 매장 격리: URL의 `{storeId}`와 JWT의 `storeId`가 일치해야 함

---

## API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | `/api/stores/{storeId}/customer/categories` | 카테고리 목록 조회 | TABLE |
| 2 | GET | `/api/stores/{storeId}/customer/menus?category={category}` | 카테고리별 메뉴 조회 | TABLE |
| 3 | GET | `/api/stores/{storeId}/customer/menus/{menuId}/spicy-options` | 메뉴 맵기 옵션 조회 | TABLE |
| 4 | POST | `/api/stores/{storeId}/customer/orders` | 주문 생성 | TABLE |
| 5 | GET | `/api/stores/{storeId}/customer/orders?page=&size=` | 주문 내역 조회 | TABLE |

---

## 1. 카테고리 목록 조회

해당 매장에 등록된 메뉴의 카테고리 목록을 조회합니다.

### 요청

```
GET /api/stores/{storeId}/customer/categories
Authorization: Bearer {JWT토큰}
```

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| storeId | Long | O | 매장 ID (JWT storeId와 일치해야 함) |

### 응답

#### 성공 (200 OK)

```json
{
  "categories": ["구이", "볶음", "분식", "밥", "음료", "찌개"]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| categories | String[] | 카테고리명 목록 (가나다순 정렬) |

### 비즈니스 로직

1. JWT에서 `storeId` 확인 (StoreAccessFilter에서 URL storeId와 일치 검증 완료)
2. 해당 매장 메뉴의 DISTINCT 카테고리 조회
3. 카테고리명 가나다순 정렬 후 반환
4. 메뉴가 없으면 빈 배열 반환

---

## 2. 카테고리별 메뉴 조회

특정 카테고리의 메뉴 목록을 조회합니다.

### 요청

```
GET /api/stores/{storeId}/customer/menus?category={category}
Authorization: Bearer {JWT토큰}
```

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| storeId | Long | O | 매장 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| category | String | O | 카테고리명 (예: "찌개") |

### 응답

#### 성공 (200 OK)

```json
{
  "menus": [
    {
      "id": 1,
      "name": "김치찌개",
      "price": 9000,
      "description": "돼지고기와 묵은지로 끓인 김치찌개",
      "category": "찌개",
      "imageUrl": "https://example.com/kimchi-jjigae.jpg",
      "spicyLevel": "매움",
      "hasSpicyOptions": true
    },
    {
      "id": 2,
      "name": "된장찌개",
      "price": 8000,
      "description": "두부와 야채가 들어간 된장찌개",
      "category": "찌개",
      "imageUrl": "https://example.com/doenjang-jjigae.jpg",
      "spicyLevel": "보통",
      "hasSpicyOptions": true
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| menus | Array | 메뉴 목록 |
| menus[].id | Long | 메뉴 ID |
| menus[].name | String | 메뉴명 |
| menus[].price | Integer | 가격 (원) |
| menus[].description | String \| null | 메뉴 설명 |
| menus[].category | String | 카테고리명 |
| menus[].imageUrl | String \| null | 이미지 URL |
| menus[].spicyLevel | String \| null | 기본 맵기 수준 (예: "매움", "보통") |
| menus[].hasSpicyOptions | Boolean | 맵기 옵션 존재 여부 (true면 API #3 호출 필요) |

### 비즈니스 로직

1. JWT에서 `storeId` 확인
2. 해당 매장 + 해당 카테고리의 메뉴 조회
3. `display_order` 기준 오름차순 정렬
4. 각 메뉴에 대해 `MenuSpicyOption` 존재 여부 확인 → `hasSpicyOptions` 설정
5. 해당 카테고리에 메뉴가 없으면 빈 배열 반환

---

## 3. 메뉴 맵기 옵션 조회

특정 메뉴의 맵기 옵션 목록을 조회합니다.

### 요청

```
GET /api/stores/{storeId}/customer/menus/{menuId}/spicy-options
Authorization: Bearer {JWT토큰}
```

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| storeId | Long | O | 매장 ID |
| menuId | Long | O | 메뉴 ID |

### 응답

#### 성공 (200 OK)

```json
{
  "options": [
    {"id": 1, "optionName": "순한맛"},
    {"id": 2, "optionName": "보통"},
    {"id": 3, "optionName": "매운맛"},
    {"id": 4, "optionName": "아주매운맛"}
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| options | Array | 맵기 옵션 목록 |
| options[].id | Long | 옵션 ID |
| options[].optionName | String | 옵션명 |

#### 메뉴 미존재 (404 Not Found)

```json
{
  "error": "MENU_NOT_FOUND",
  "message": "존재하지 않는 메뉴입니다 (menuId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

1. JWT에서 `storeId` 확인
2. `menuId`로 메뉴 조회 → 없으면 `MENU_NOT_FOUND`
3. 메뉴의 `storeId`가 JWT `storeId`와 일치하는지 확인 → 불일치 시 `MENU_NOT_FOUND`
4. 해당 메뉴의 맵기 옵션을 `display_order` 기준 오름차순 조회
5. 옵션이 없으면 빈 배열 반환

---

## 4. 주문 생성

장바구니의 메뉴를 주문합니다. 첫 주문 시 세션이 자동 생성됩니다.

### 요청

```
POST /api/stores/{storeId}/customer/orders
Content-Type: application/json
Authorization: Bearer {JWT토큰}
```

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| storeId | Long | O | 매장 ID |

#### Request Body

```json
{
  "items": [
    {
      "menuId": 1,
      "quantity": 2,
      "unitPrice": 9000,
      "spicyOption": "보통",
      "specialRequest": "청양고추 빼주세요"
    },
    {
      "menuId": 5,
      "quantity": 1,
      "unitPrice": 13000,
      "spicyOption": null,
      "specialRequest": null
    }
  ]
}
```

#### 필드 상세

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| items | Array | O | 1개 이상 | 주문 항목 목록 |
| items[].menuId | Long | O | 양의 정수 | 메뉴 ID |
| items[].quantity | Integer | O | 1 이상 | 수량 |
| items[].unitPrice | Integer | O | 양의 정수 | 단가 (DB 가격과 일치해야 함) |
| items[].spicyOption | String | X | - | 맵기 옵션명 (null 허용) |
| items[].specialRequest | String | X | - | 요청사항 (null 허용) |

### 응답

#### 성공 (201 Created)

```json
{
  "orderId": 42,
  "orderNumber": "20260306-001",
  "totalAmount": 31000,
  "status": "WAITING",
  "createdAt": "2026-03-06T15:30:00+09:00",
  "sessionId": 10,
  "items": [
    {
      "menuName": "김치찌개",
      "quantity": 2,
      "unitPrice": 9000,
      "spicyOption": "보통",
      "specialRequest": "청양고추 빼주세요"
    },
    {
      "menuName": "삼겹살",
      "quantity": 1,
      "unitPrice": 13000,
      "spicyOption": null,
      "specialRequest": null
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Long | 주문 ID |
| orderNumber | String | 주문 번호 (형식: yyyyMMdd-NNN) |
| totalAmount | Integer | 총 금액 = Σ(unitPrice × quantity) |
| status | String | 주문 상태 (항상 `WAITING`) |
| createdAt | String | 주문 생성 시각 (ISO 8601) |
| sessionId | Long | 세션 ID (신규 생성된 경우 새 ID) |
| items | Array | 주문 항목 목록 |
| items[].menuName | String | 메뉴명 (주문 시점 스냅샷) |
| items[].quantity | Integer | 수량 |
| items[].unitPrice | Integer | 단가 (주문 시점 스냅샷) |
| items[].spicyOption | String \| null | 맵기 옵션 |
| items[].specialRequest | String \| null | 요청사항 |

#### 입력 검증 실패 (400 Bad Request)

```json
{
  "error": "VALIDATION_FAILED",
  "message": "주문 항목이 비어있습니다",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

#### 잘못된 맵기 옵션 (400 Bad Request)

```json
{
  "error": "INVALID_SPICY_OPTION",
  "message": "유효하지 않은 맵기 옵션입니다 (김치찌개: 극한맛)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

#### 메뉴 미존재 (404 Not Found)

```json
{
  "error": "MENU_NOT_FOUND",
  "message": "존재하지 않는 메뉴입니다 (menuId: 999)",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

#### 세션 미존재 (404 Not Found)

```json
{
  "error": "SESSION_NOT_FOUND",
  "message": "유효하지 않은 세션입니다",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

#### 가격 불일치 (409 Conflict)

```json
{
  "error": "PRICE_MISMATCH",
  "message": "메뉴 가격이 변경되었습니다. 장바구니를 확인해주세요",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```

### 비즈니스 로직

```
1. JWT에서 storeId, tableId, sessionId 추출

2. 주문 항목 검증 (각 item에 대해):
   a. menuId로 Menu 조회 → 없으면 MENU_NOT_FOUND
   b. Menu.storeId == JWT.storeId 확인 → 불일치 시 MENU_NOT_FOUND
   c. item.unitPrice vs Menu.price 비교 → 불일치 시 PRICE_MISMATCH
   d. spicyOption이 있으면 MenuSpicyOption에 등록된 옵션인지 확인
      → 미등록이면 INVALID_SPICY_OPTION
   e. quantity > 0 확인

3. 세션 처리:
   if (sessionId == null):
     → TableSession 생성 (storeId, tableId, sessionCode=UUID, active=true)
     → 생성된 sessionId 사용
   else:
     → sessionId로 활성 세션 조회
     → 없으면 SESSION_NOT_FOUND

4. 주문 번호 채번 (낙관적 재시도, 최대 3회):
   → datePrefix = LocalDate.now().format("yyyyMMdd")
   → count = 당일 해당 매장 주문 수 조회
   → orderNumber = "{datePrefix}-{(count+1) 3자리 zero-pad}"
   → UNIQUE 위반 시 재시도

5. Order 생성:
   → storeId, tableId, sessionId, orderNumber
   → totalAmount = Σ(unitPrice × quantity)
   → status = "WAITING"

6. OrderItem 생성 (각 item):
   → menuName = DB에서 조회한 현재 메뉴명 (스냅샷)
   → unitPrice = 클라이언트 전달 값 (검증 완료)

7. SSE 이벤트 발행 (fire-and-forget):
   → sseService.publishNewOrder(storeId, orderDTO)
   → 실패 시 로그만 남기고 주문은 정상 완료

8. 응답 반환 (201 Created)
```

### 트랜잭션 범위

- 단계 2~6: 단일 `@Transactional` — 어느 단계에서든 실패 시 전체 롤백
- 단계 7: 트랜잭션 커밋 후 별도 실행 — SSE 실패가 주문에 영향 없음

### 참고사항

- 주문 번호 형식: `yyyyMMdd-NNN` (매장별 + 당일 기준, 예: 20260306-001)
- 가격 불일치 시 관리자에게도 SSE로 `MENU_UPDATED` 이벤트 발행 가능 (Unit 5 연계)
- 세션 자동 생성 시 JWT에는 이전 sessionId(null)가 남아있으므로, 응답의 `sessionId`를 클라이언트가 저장해야 함

---

## 5. 주문 내역 조회

현재 세션의 주문 내역을 페이지네이션으로 조회합니다.

### 요청

```
GET /api/stores/{storeId}/customer/orders?page={page}&size={size}
Authorization: Bearer {JWT토큰}
```

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| storeId | Long | O | 매장 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | Integer | X | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | X | 10 | 페이지 크기 |

### 응답

#### 성공 (200 OK) - 주문 있음

```json
{
  "orders": [
    {
      "orderId": 42,
      "orderNumber": "20260306-002",
      "totalAmount": 31000,
      "status": "PREPARING",
      "createdAt": "2026-03-06T15:45:00+09:00",
      "items": [
        {
          "menuName": "김치찌개",
          "quantity": 2,
          "unitPrice": 9000,
          "spicyOption": "보통",
          "specialRequest": "청양고추 빼주세요"
        },
        {
          "menuName": "삼겹살",
          "quantity": 1,
          "unitPrice": 13000,
          "spicyOption": null,
          "specialRequest": null
        }
      ]
    },
    {
      "orderId": 41,
      "orderNumber": "20260306-001",
      "totalAmount": 8000,
      "status": "DONE",
      "createdAt": "2026-03-06T15:30:00+09:00",
      "items": [
        {
          "menuName": "된장찌개",
          "quantity": 1,
          "unitPrice": 8000,
          "spicyOption": "순한맛",
          "specialRequest": null
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orders | Array | 주문 목록 (최신순 정렬) |
| orders[].orderId | Long | 주문 ID |
| orders[].orderNumber | String | 주문 번호 |
| orders[].totalAmount | Integer | 총 금액 |
| orders[].status | String | 주문 상태 (`WAITING` / `PREPARING` / `DONE`) |
| orders[].createdAt | String | 주문 생성 시각 (ISO 8601) |
| orders[].items | Array | 주문 항목 목록 |
| orders[].items[].menuName | String | 메뉴명 (주문 시점 스냅샷) |
| orders[].items[].quantity | Integer | 수량 |
| orders[].items[].unitPrice | Integer | 단가 (주문 시점 스냅샷) |
| orders[].items[].spicyOption | String \| null | 맵기 옵션 |
| orders[].items[].specialRequest | String \| null | 요청사항 |
| page | Integer | 현재 페이지 번호 |
| size | Integer | 페이지 크기 |
| totalElements | Long | 전체 주문 수 |
| totalPages | Integer | 전체 페이지 수 |

#### 성공 (200 OK) - 세션 없음 (sessionId == null)

```json
{
  "orders": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0
}
```

### 비즈니스 로직

1. JWT에서 `storeId`, `sessionId` 추출
2. `sessionId`가 null이면 빈 목록 반환 (에러 아님)
3. 해당 세션의 주문을 `createdAt` 내림차순으로 페이지네이션 조회
4. 각 주문에 대해 `OrderItem` 목록 조회
5. 페이지 정보와 함께 반환

### 참고사항

- 주문 상태는 고객이 변경할 수 없음 (관리자만 변경 가능, Unit 5)
- 세션이 없는 상태(첫 방문, 주문 전)에서 조회하면 빈 목록 반환

---

## JWT 토큰 클레임 참조 (Unit 2 정의)

Unit 3의 모든 API는 테이블 로그인 토큰의 다음 클레임을 사용합니다:

| 클레임 | 타입 | 용도 |
|--------|------|------|
| storeId | Long | 매장 격리 (StoreAccessFilter에서 검증) |
| tableId | Long | 주문 생성 시 테이블 식별 |
| sessionId | Long \| null | 세션 기반 주문 조회, null이면 세션 자동 생성 |
| role | String | `TABLE` — 접근 제어 |

---

## SSE 이벤트 연계 (Unit 5)

주문 생성 성공 시 관리자 화면에 실시간 알림을 위한 SSE 이벤트를 발행합니다.

| 이벤트 | 발행 시점 | 데이터 | 구독 대상 |
|--------|-----------|--------|-----------|
| `NEW_ORDER` | 주문 생성 성공 후 | 주문 요약 정보 | 해당 매장 관리자 |

- 발행 방식: Fire-and-Forget (실패 시 로그만 남김)
- 안전망: 관리자 화면의 주기적 polling (30초~1분, Unit 6에서 구현)
