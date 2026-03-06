# TDD Code Generation Plan - Unit 4: 고객 프론트엔드

## Unit Context
- **Workspace Root**: D:\workspace\kr-kiro-workshop
- **Project Type**: Greenfield (Frontend)
- **Stories**: US-C01~C08
- **Code Location**: `frontend/customer/` (개발) → `backend/src/main/resources/static/customer/` (배포)
- **Test Location**: `frontend/customer/tests/`

---

### Plan Step 0: 프로젝트 구조 + Contract Skeleton 생성
- [x] `frontend/customer/index.html` - SPA 진입점 (Bootstrap 5 CDN, View 컨테이너)
- [x] `frontend/customer/css/style.css` - 커스텀 스타일
- [x] `frontend/customer/js/api.js` - ApiClient skeleton (NotImplemented)
- [x] `frontend/customer/js/auth.js` - Auth skeleton (NotImplemented)
- [x] `frontend/customer/js/cart.js` - Cart skeleton (NotImplemented)
- [x] `frontend/customer/js/menu.js` - MenuView skeleton (NotImplemented)
- [x] `frontend/customer/js/order.js` - OrderView skeleton (NotImplemented)
- [x] `frontend/customer/js/order-history.js` - OrderHistoryView skeleton (NotImplemented)
- [x] `frontend/customer/js/app.js` - App 초기화 + Router skeleton
- [x] `frontend/customer/tests/test-utils.js` - 테스트 유틸 (assert, mock)
- [x] `frontend/customer/tests/run-tests.html` - 테스트 실행 HTML
- [x] 브라우저에서 skeleton 로드 확인

### Plan Step 1: Cart 모듈 TDD (9 TC)
- [x] Cart.addItem() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-001 (빈 장바구니에 추가)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-002 (기존 메뉴 수량 증가)
  - [x] GREEN: 중복 체크 로직 추가
  - [x] VERIFY: TC-FE-001~002 PASSED
- [x] Cart.updateQuantity() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-003 (수량 +1)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-004 (수량 -1)
  - [x] GREEN: 감소 로직
  - [x] RED: TC-FE-005 (수량 0 자동 제거)
  - [x] GREEN: 제거 로직
  - [x] VERIFY: TC-FE-003~005 PASSED
- [x] Cart.removeItem() + Cart.clear() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-006 (항목 삭제)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-007 (전체 비우기)
  - [x] GREEN: 최소 구현
  - [x] VERIFY: TC-FE-006~007 PASSED
- [x] Cart.getTotalAmount() + getTotalCount() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-008 (총 금액)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-009 (총 수량)
  - [x] GREEN: 최소 구현
  - [x] VERIFY: TC-FE-008~009 PASSED
- [x] REFACTOR: Cart 모듈 전체 리팩토링

### Plan Step 2: API + Auth 모듈 TDD (8 TC)
- [x] ApiClient.request() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-010 (성공 응답)
  - [x] GREEN: 최소 fetch wrapper
  - [x] RED: TC-FE-011 (X-New-Token 감지)
  - [x] GREEN: 토큰 교체 로직
  - [x] RED: TC-FE-012 (401 에러)
  - [x] GREEN: 에러 throw
  - [x] RED: TC-FE-013 (에러 파싱)
  - [x] GREEN: 에러 객체 구성
  - [x] VERIFY: TC-FE-010~013 PASSED
- [x] Auth.login() + isLoggedIn() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-014 (로그인 성공)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-015 (로그인 실패)
  - [x] GREEN: 에러 처리
  - [x] RED: TC-FE-016 (토큰 있으면 true)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-017 (토큰 없으면 false)
  - [x] GREEN: 최소 구현
  - [x] VERIFY: TC-FE-014~017 PASSED
- [x] REFACTOR: API + Auth 모듈 전체 리팩토링

### Plan Step 3: UI 모듈 구현 (Menu, Order, OrderHistory, App)
- [x] OrderHistoryView.getStatusBadge() - RED-GREEN-REFACTOR
  - [x] RED: TC-FE-018 (WAITING)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-FE-019 (PREPARING)
  - [x] GREEN: 추가
  - [x] RED: TC-FE-020 (DONE)
  - [x] GREEN: 추가
  - [x] VERIFY: TC-FE-018~020 PASSED
- [x] MenuView 전체 구현 (render, loadCategories, loadMenus, renderMenuCard)
- [x] CartPanel 사이드 패널 구현 (offcanvas 렌더링, 이벤트 바인딩)
- [x] OrderView 전체 구현 (render, loadSpicyOptions, submitOrder)
- [x] OrderHistoryView 전체 구현 (render, loadOrders, 무한 스크롤)
- [x] App 초기화 + Router 구현 (init, navigate, showView, showToast, showSpinner)

### Plan Step 4: 통합 + Security 설정 + 문서화
- [x] Spring Security에 `/customer/**` permitAll 추가
- [x] `frontend/customer/` → `backend/src/main/resources/static/customer/` 복사
- [ ] 전체 테스트 실행 (20 TC 확인) - 브라우저 기반 수동 확인 필요
- [x] `aidlc-docs/construction/unit-4-customer-fe/code/code-summary.md` 생성
- [x] `aidlc-state.md` 업데이트
