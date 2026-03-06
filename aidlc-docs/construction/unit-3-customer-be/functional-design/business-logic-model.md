# Unit 3: Customer Backend - Business Logic Model

## API 엔드포인트 설계

| # | Method | Endpoint | 설명 | Story |
|---|--------|----------|------|-------|
| 1 | GET | `/api/stores/{storeId}/customer/categories` | 카테고리 목록 조회 | US-C02 |
| 2 | GET | `/api/stores/{storeId}/customer/menus?category={category}` | 카테고리별 메뉴 조회 | US-C02 |
| 3 | GET | `/api/stores/{storeId}/customer/menus/{menuId}/spicy-options` | 메뉴 맵기 옵션 조회 | US-C05 |
| 4 | POST | `/api/stores/{storeId}/customer/orders` | 주문 생성 | US-C05 |
| 5 | GET | `/api/stores/{storeId}/customer/orders` | 주문 내역 조회 | US-C06, C07, C08 |

---

## Flow 1: 카테고리 목록 조회 (GET /customer/categories)

```
Request → JWT에서 storeId 확인
  → MenuRepository.findDistinctCategoryByStoreId(storeId)
  → 카테고리 문자열 목록 반환
```

### Response
```json
{
  "categories": ["찌개", "볶음", "밥", "구이", "분식", "음료"]
}
```

---

## Flow 2: 카테고리별 메뉴 조회 (GET /customer/menus?category=찌개)

```
Request → JWT에서 storeId 확인
  → category 파라미터 확인
  → MenuRepository.findByStoreIdAndCategoryOrderByDisplayOrder(storeId, category)
  → MenuDTO 목록 반환 (spicyLevel 포함, spicyOptions 미포함)
```

### Response
```json
{
  "menus": [
    {
      "id": 1,
      "name": "김치찌개",
      "price": 9000,
      "description": "돼지고기와 묵은지로 끓인 김치찌개",
      "category": "찌개",
      "imageUrl": "https://...",
      "spicyLevel": "매움",
      "hasSpicyOptions": true
    }
  ]
}
```

- `hasSpicyOptions`: 해당 메뉴에 맵기 옵션이 등록되어 있는지 여부 (프론트에서 옵션 조회 필요 여부 판단)

---

## Flow 3: 메뉴 맵기 옵션 조회 (GET /customer/menus/{menuId}/spicy-options)

```
Request → JWT에서 storeId 확인
  → Menu 존재 여부 + storeId 일치 확인
  → MenuSpicyOptionRepository.findByMenuIdOrderByDisplayOrder(menuId)
  → 옵션 목록 반환
```

### Response
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

---

## Flow 4: 주문 생성 (POST /customer/orders)

### Request Body
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

### 처리 플로우

```
1. JWT에서 storeId, tableId, sessionId 추출

2. 주문 항목 검증 (각 item에 대해):
   a. Menu 존재 여부 확인 → 없으면 MENU_NOT_FOUND 에러
   b. Menu.storeId == JWT.storeId 확인
   c. 클라이언트 unitPrice vs DB Menu.price 비교
      → 불일치 시 PRICE_MISMATCH 에러 (변경된 메뉴 정보 포함)
   d. spicyOption이 있으면 MenuSpicyOption에 등록된 옵션인지 확인
      → 미등록 옵션이면 INVALID_SPICY_OPTION 에러
   e. quantity > 0 확인

3. 세션 처리:
   if (sessionId == null)
     → TableSession 생성 (storeId, tableId, sessionCode=UUID, active=true)
     → 생성된 sessionId 사용
   else
     → 기존 sessionId의 활성 세션 존재 확인
     → 없으면 SESSION_NOT_FOUND 에러

4. 주문 번호 채번:
   → datePrefix = 오늘 날짜 (yyyyMMdd)
   → 당일 해당 매장 주문 수 조회
   → orderNumber = "{datePrefix}-{순번 3자리 zero-pad}"
   → 예: "20260306-001"

5. Order 생성:
   → storeId, tableId, sessionId, orderNumber
   → totalAmount = Σ(unitPrice × quantity)
   → status = "WAITING"

6. OrderItem 생성 (각 item):
   → orderId, menuId, menuName(DB에서 조회), quantity, unitPrice(클라이언트 값)
   → spicyOption, specialRequest

7. SSE 이벤트 발행 (Unit 5 연계):
   → publishNewOrder(storeId, orderDTO)

8. 응답 반환
```

### Response (201 Created)
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
    }
  ]
}
```

### 에러 응답

| 상황 | Error Code | Message |
|------|------------|---------|
| 메뉴 미존재 | MENU_NOT_FOUND | 존재하지 않는 메뉴입니다 (menuId: {id}) |
| 가격 불일치 | PRICE_MISMATCH | 메뉴 가격이 변경되었습니다. 장바구니를 확인해주세요 |
| 잘못된 맵기 옵션 | INVALID_SPICY_OPTION | 유효하지 않은 맵기 옵션입니다 ({menuName}: {option}) |
| 세션 미존재 | SESSION_NOT_FOUND | 유효하지 않은 세션입니다 |
| 빈 주문 | VALIDATION_FAILED | 주문 항목이 비어있습니다 |

---

## Flow 5: 주문 내역 조회 (GET /customer/orders?page=0&size=10)

```
1. JWT에서 storeId, tableId, sessionId 추출

2. sessionId 처리:
   if (sessionId == null)
     → 빈 목록 반환 (주문 없음)
   else
     → OrderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, pageable)

3. 각 Order에 대해 OrderItem 목록 조회

4. 페이지 정보와 함께 반환
```

### Response
```json
{
  "orders": [
    {
      "orderId": 42,
      "orderNumber": "20260306-001",
      "totalAmount": 31000,
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
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```
