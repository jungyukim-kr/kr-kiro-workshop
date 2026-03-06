# Contract/Interface Definition - Unit 4: 고객 프론트엔드

## Unit Context
- **Stories**: US-C01~C08 (테이블 자동 로그인, 메뉴 조회, 장바구니, 주문 생성, 주문 내역)
- **Dependencies**: Unit 2 (Auth API), Unit 3 (Customer API)
- **Tech Stack**: Vanilla JS + Bootstrap 5 CDN
- **Test Framework**: 자체 테스트 유틸 (assert 함수 + 브라우저 콘솔)

---

## API Layer (api.js)

### ApiClient
- `request(method, endpoint, body?) -> Promise<Object>`: API 호출 공통 wrapper
  - Args: method(String), endpoint(String), body(Object|null)
  - Returns: JSON 응답 객체
  - Throws: ApiError (status, error, message)
  - 기능: Authorization 헤더 주입, X-New-Token 감지, 401 재로그인, 타임아웃 10초

- `get(endpoint) -> Promise<Object>`: GET 요청 shortcut
- `post(endpoint, body) -> Promise<Object>`: POST 요청 shortcut

### 상수
- `BASE_URL`: API base URL (예: `http://localhost:8080`)

---

## Auth Layer (auth.js)

### Auth
- `init() -> Promise<void>`: 앱 시작 시 자동 로그인 시도
  - localStorage 확인 → validate → 실패 시 재로그인 → 실패 시 로그인 화면
  - Story: US-C01

- `login(storeCode, tableNumber, password) -> Promise<Object>`: 테이블 로그인
  - Args: storeCode(String), tableNumber(Number), password(String)
  - Returns: TokenResponse
  - Throws: ApiError
  - Story: US-C01

- `validateToken() -> Promise<Object|null>`: 토큰 유효성 확인
  - Returns: ValidateResponse 또는 null (실패 시)
  - Story: US-C01

- `getToken() -> String|null`: localStorage에서 토큰 조회
- `getStoreId() -> Number|null`: localStorage에서 storeId 조회
- `saveAuth(response) -> void`: 로그인 응답을 localStorage에 저장
- `clearAuth() -> void`: localStorage 인증 정보 삭제
- `isLoggedIn() -> Boolean`: 로그인 상태 확인

---

## Menu Layer (menu.js)

### MenuView
- `render() -> void`: 메뉴 화면 렌더링
  - 카테고리 로드 → 첫 카테고리 메뉴 로드
  - Story: US-C02

- `loadCategories() -> Promise<void>`: 카테고리 목록 로드 및 탭 렌더링
  - Story: US-C02

- `loadMenus(category) -> Promise<void>`: 카테고리별 메뉴 로드 및 카드 렌더링
  - Args: category(String)
  - Story: US-C02

- `renderMenuCard(menu) -> HTMLElement`: 메뉴 카드 DOM 생성
  - Args: menu(Object)
  - Returns: 카드 DOM 요소
  - Story: US-C02, US-C03

---

## Cart Layer (cart.js)

### Cart
- `getItems() -> CartItem[]`: 장바구니 항목 조회 (localStorage)
  - Story: US-C03

- `addItem(menu) -> void`: 메뉴 추가 (신규 또는 수량 +1)
  - Args: menu(Object {id, name, price, hasSpicyOptions})
  - Story: US-C03

- `updateQuantity(menuId, delta) -> void`: 수량 변경 (+1/-1)
  - Args: menuId(Number), delta(Number)
  - 수량 0이면 자동 제거
  - Story: US-C04

- `removeItem(menuId) -> void`: 항목 삭제
  - Args: menuId(Number)
  - Story: US-C04

- `clear() -> void`: 장바구니 비우기
  - Story: US-C04

- `getTotalAmount() -> Number`: 총 금액 계산
  - Returns: Σ(unitPrice × quantity)
  - Story: US-C03, US-C04

- `getTotalCount() -> Number`: 총 수량 계산
  - Story: US-C03

- `getItemQuantity(menuId) -> Number`: 특정 메뉴 수량 조회
  - Story: US-C03

---

## Order Layer (order.js)

### OrderView
- `render() -> void`: 주문 확인 화면 렌더링
  - 장바구니 항목 표시 + 맵기 옵션 로드
  - Story: US-C05

- `loadSpicyOptions(menuId) -> Promise<SpicyOption[]>`: 맵기 옵션 조회
  - Story: US-C05

- `submitOrder() -> Promise<void>`: 주문 생성
  - CartItem[] → OrderRequest 변환 → POST /orders
  - 성공: 토스트 + 장바구니 비우기 + 5초 후 메뉴 이동
  - 실패: 에러 토스트
  - Story: US-C05

---

## Order History Layer (order-history.js)

### OrderHistoryView
- `render() -> void`: 주문 내역 화면 렌더링
  - Story: US-C06

- `loadOrders(page) -> Promise<void>`: 주문 내역 페이지 로드
  - Args: page(Number)
  - Story: US-C06, US-C08

- `renderOrderCard(order) -> HTMLElement`: 주문 카드 DOM 생성
  - Args: order(Object)
  - Story: US-C06, US-C07

- `getStatusBadge(status) -> Object`: 상태별 뱃지 정보
  - Args: status(String: WAITING|PREPARING|DONE)
  - Returns: {text, cssClass}
  - Story: US-C07

---

## App Layer (app.js)

### App
- `init() -> void`: 앱 초기화 (DOMContentLoaded)
- `navigate(hash) -> void`: 화면 전환
- `showView(viewName) -> void`: View 표시/숨김 전환
- `showToast(message, type) -> void`: 토스트 알림 표시
- `showSpinner() / hideSpinner() -> void`: 로딩 스피너 표시/숨김
