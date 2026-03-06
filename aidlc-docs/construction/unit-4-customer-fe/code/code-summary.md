# Code Summary - Unit 4: 고객 프론트엔드

## 개요
테이블오더 고객용 SPA (Single Page Application). Vanilla JavaScript + Bootstrap 5 기반.
Hash Router로 View 전환, localStorage 기반 장바구니, JWT 인증 연동.

## 파일 구조

```
frontend/customer/              # 개발 소스
├── index.html                  # SPA 진입점 (Bootstrap 5 CDN)
├── css/style.css               # 커스텀 스타일
├── js/
│   ├── api.js                  # ApiClient - fetch wrapper, JWT, X-New-Token
│   ├── auth.js                 # Auth - 로그인, 자동 로그인, 토큰 관리
│   ├── cart.js                 # Cart - localStorage CRUD, 금액/수량 계산
│   ├── menu.js                 # MenuView - 카테고리/메뉴 조회, 카드 렌더링
│   ├── order.js                # OrderView - 맵기 옵션, 요청사항, 주문 전송
│   ├── order-history.js        # OrderHistoryView - 주문 조회, 무한 스크롤
│   └── app.js                  # App - Router, Toast, Spinner, Cart Panel
└── tests/
    ├── test-utils.js           # 테스트 유틸 (assert, mock)
    ├── run-tests.html          # 브라우저 테스트 실행기
    ├── cart.test.js            # Cart 9 TC
    ├── api.test.js             # Api 4 TC
    ├── auth.test.js            # Auth 4 TC
    └── order-history.test.js   # OrderHistoryView 3 TC

backend/src/main/resources/static/customer/  # 배포 위치 (tests 제외)
```

## TDD 결과
- 총 20 TC (Cart 9 + Api 4 + Auth 4 + OrderHistory 3)
- 브라우저 기반 테스트 (`frontend/customer/tests/run-tests.html`)

## 모듈 요약

| 모듈 | 역할 | 주요 함수 |
|------|------|-----------|
| Api | HTTP 통신 | request(), get(), post() |
| Auth | 인증 관리 | login(), init(), isLoggedIn(), validateToken() |
| Cart | 장바구니 | addItem(), updateQuantity(), removeItem(), clear() |
| MenuView | 메뉴 화면 | render(), loadCategories(), loadMenus() |
| OrderView | 주문 화면 | render(), submitOrder(), loadSpicyOptions() |
| OrderHistoryView | 주문 내역 | render(), loadOrders(), getStatusBadge() |
| App | 앱 코어 | init(), navigate(), showView(), showToast() |

## Spring Security 변경
- `SecurityConfig.java`에 `.requestMatchers("/customer/**").permitAll()` 추가
- 정적 리소스 인증 없이 접근 가능

## API 연동 (Unit 3 Customer BE)
- `POST /api/auth/table/login` - 테이블 로그인
- `GET /api/auth/validate` - 토큰 검증
- `GET /api/stores/{id}/customer/categories` - 카테고리 목록
- `GET /api/stores/{id}/customer/menus?category=` - 메뉴 목록
- `GET /api/stores/{id}/customer/menus/{menuId}/spicy-options` - 맵기 옵션
- `POST /api/stores/{id}/customer/orders` - 주문 생성
- `GET /api/stores/{id}/customer/orders?page=&size=` - 주문 내역
