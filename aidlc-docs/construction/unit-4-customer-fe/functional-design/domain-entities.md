# Domain Entities - Unit 4: 고객 프론트엔드

## 클라이언트 데이터 모델

Frontend에서 관리하는 데이터 모델과 localStorage 구조를 정의합니다.

---

## 1. localStorage 구조

### 인증 정보 (auth)

| Key | 타입 | 설명 | 예시 |
|-----|------|------|------|
| `auth_storeCode` | String | 매장 식별 코드 | `"STORE001"` |
| `auth_tableNumber` | Number | 테이블 번호 | `5` |
| `auth_password` | String | 4자리 PIN | `"1234"` |
| `auth_token` | String | JWT 토큰 | `"eyJhbG..."` |
| `auth_storeId` | Number | 매장 ID | `1` |
| `auth_tableId` | Number | 테이블 ID | `5` |
| `auth_sessionId` | Number/null | 세션 ID | `10` |

### 장바구니 (cart)

| Key | 타입 | 설명 |
|-----|------|------|
| `cart_items` | JSON String | 장바구니 항목 배열 (CartItem[]) |

---

## 2. 클라이언트 엔티티

### AuthInfo

```
AuthInfo {
  storeCode: String       // 매장 식별 코드
  tableNumber: Number     // 테이블 번호
  password: String        // 4자리 PIN
  token: String           // JWT 토큰
  storeId: Number         // 매장 ID
  tableId: Number         // 테이블 ID
  sessionId: Number|null  // 세션 ID (첫 주문 전 null 가능)
}
```

### Category

```
Category {
  name: String            // 카테고리명 (예: "찌개")
}
```

### Menu

```
Menu {
  id: Number              // 메뉴 ID
  name: String            // 메뉴명
  price: Number           // 가격 (원)
  description: String|null // 메뉴 설명
  category: String        // 카테고리명
  imageUrl: String|null   // 이미지 URL
  spicyLevel: String|null // 기본 맵기 수준
  hasSpicyOptions: Boolean // 맵기 옵션 존재 여부
}
```

### SpicyOption

```
SpicyOption {
  id: Number              // 옵션 ID
  optionName: String      // 옵션명 (예: "순한맛")
}
```

### CartItem

```
CartItem {
  menuId: Number          // 메뉴 ID
  menuName: String        // 메뉴명 (표시용)
  unitPrice: Number       // 단가
  quantity: Number         // 수량 (1 이상)
  hasSpicyOptions: Boolean // 맵기 옵션 존재 여부
  spicyOption: String|null // 선택한 맵기 옵션 (주문 확인 시 설정)
  specialRequest: String|null // 요청사항 (주문 확인 시 설정)
}
```

### OrderRequest (API 전송용)

```
OrderRequest {
  items: OrderItemRequest[]
}

OrderItemRequest {
  menuId: Number          // 메뉴 ID
  quantity: Number         // 수량
  unitPrice: Number        // 단가
  spicyOption: String|null // 맵기 옵션명
  specialRequest: String|null // 요청사항
}
```

### OrderResponse (API 응답)

```
OrderResponse {
  orderId: Number         // 주문 ID
  orderNumber: String     // 주문 번호 (yyyyMMdd-NNN)
  totalAmount: Number     // 총 금액
  status: String          // 주문 상태 (WAITING)
  createdAt: String       // 생성 시각 (ISO 8601)
  sessionId: Number       // 세션 ID
  items: OrderItemResponse[]
}

OrderItemResponse {
  menuName: String        // 메뉴명
  quantity: Number         // 수량
  unitPrice: Number        // 단가
  spicyOption: String|null
  specialRequest: String|null
}
```

### OrderHistoryResponse (주문 내역 API 응답)

```
OrderHistoryResponse {
  orders: OrderSummary[]
  page: Number            // 현재 페이지
  size: Number            // 페이지 크기
  totalElements: Number   // 전체 주문 수
  totalPages: Number      // 전체 페이지 수
}

OrderSummary {
  orderId: Number
  orderNumber: String
  totalAmount: Number
  status: String          // WAITING | PREPARING | DONE
  createdAt: String
  items: OrderItemResponse[]
}
```

---

## 3. 엔티티 관계도

```
AuthInfo ──── localStorage 저장/로드
    │
    ├── token → API 요청 시 Authorization 헤더
    ├── storeId → API URL path parameter
    └── sessionId → 주문 생성 시 세션 자동 생성 후 업데이트

Category[] ←── GET /categories 응답
    │
    └── name → GET /menus?category={name} 파라미터

Menu[] ←── GET /menus?category= 응답
    │
    ├── id → CartItem.menuId
    ├── hasSpicyOptions → SpicyOption 조회 여부 결정
    └── price → CartItem.unitPrice

SpicyOption[] ←── GET /menus/{menuId}/spicy-options 응답

CartItem[] ──── localStorage 저장/로드
    │
    └── → OrderRequest 변환 → POST /orders

OrderResponse ←── POST /orders 응답
    │
    └── sessionId → AuthInfo.sessionId 업데이트

OrderHistoryResponse ←── GET /orders?page=&size= 응답
```
