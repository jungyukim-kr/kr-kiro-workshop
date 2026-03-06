# Test Plan - Unit 4: 고객 프론트엔드

## Unit Overview
- **Unit**: unit-4-customer-fe
- **Stories**: US-C01~C08
- **Test Framework**: 자체 테스트 유틸 (test-utils.js: assert, mock fetch)
- **Test 실행**: 브라우저 콘솔 또는 Node.js (DOM 미사용 로직)

---

## Cart 모듈 테스트 (cart.js)

### Cart.addItem()
- **TC-FE-001**: 빈 장바구니에 메뉴 추가 시 수량 1로 추가
  - Given: 장바구니가 비어있음
  - When: addItem({id:1, name:"김치찌개", price:9000, hasSpicyOptions:true})
  - Then: getItems().length === 1, getItems()[0].quantity === 1
  - Story: US-C03
  - Status: ⬜ Not Started

- **TC-FE-002**: 이미 있는 메뉴 추가 시 수량 +1
  - Given: 장바구니에 menuId=1 수량 1 존재
  - When: addItem({id:1, ...})
  - Then: getItems()[0].quantity === 2
  - Story: US-C03
  - Status: ⬜ Not Started

### Cart.updateQuantity()
- **TC-FE-003**: 수량 +1 증가
  - Given: 장바구니에 menuId=1 수량 2 존재
  - When: updateQuantity(1, 1)
  - Then: getItemQuantity(1) === 3
  - Story: US-C04
  - Status: ⬜ Not Started

- **TC-FE-004**: 수량 -1 감소
  - Given: 장바구니에 menuId=1 수량 2 존재
  - When: updateQuantity(1, -1)
  - Then: getItemQuantity(1) === 1
  - Story: US-C04
  - Status: ⬜ Not Started

- **TC-FE-005**: 수량 0이면 자동 제거
  - Given: 장바구니에 menuId=1 수량 1 존재
  - When: updateQuantity(1, -1)
  - Then: getItems().length === 0
  - Story: US-C04
  - Status: ⬜ Not Started

### Cart.removeItem()
- **TC-FE-006**: 항목 삭제
  - Given: 장바구니에 menuId=1 존재
  - When: removeItem(1)
  - Then: getItems().length === 0
  - Story: US-C04
  - Status: ⬜ Not Started

### Cart.clear()
- **TC-FE-007**: 장바구니 비우기
  - Given: 장바구니에 3개 항목 존재
  - When: clear()
  - Then: getItems().length === 0, getTotalAmount() === 0
  - Story: US-C04
  - Status: ⬜ Not Started

### Cart.getTotalAmount()
- **TC-FE-008**: 총 금액 계산
  - Given: 장바구니에 {price:9000, qty:2}, {price:13000, qty:1}
  - When: getTotalAmount()
  - Then: 31000
  - Story: US-C03
  - Status: ⬜ Not Started

### Cart.getTotalCount()
- **TC-FE-009**: 총 수량 계산
  - Given: 장바구니에 {qty:2}, {qty:1}
  - When: getTotalCount()
  - Then: 3
  - Story: US-C03
  - Status: ⬜ Not Started

---

## API 모듈 테스트 (api.js)

### ApiClient.request()
- **TC-FE-010**: 성공 응답 처리
  - Given: mock fetch가 200 + JSON 반환
  - When: request("GET", "/test")
  - Then: JSON 객체 반환
  - Story: 공통
  - Status: ⬜ Not Started

- **TC-FE-011**: X-New-Token 헤더 감지 시 토큰 교체
  - Given: mock fetch 응답에 X-New-Token 헤더 포함
  - When: request("GET", "/test")
  - Then: localStorage의 auth_token이 새 토큰으로 교체
  - Story: US-C01 (BR-AUTH-03)
  - Status: ⬜ Not Started

- **TC-FE-012**: 401 응답 시 에러 throw
  - Given: mock fetch가 401 반환
  - When: request("GET", "/test")
  - Then: ApiError throw (status=401)
  - Story: 공통
  - Status: ⬜ Not Started

- **TC-FE-013**: 에러 응답 파싱
  - Given: mock fetch가 400 + {error, message} 반환
  - When: request("POST", "/test", body)
  - Then: ApiError에 error, message 포함
  - Story: 공통
  - Status: ⬜ Not Started

---

## Auth 모듈 테스트 (auth.js)

### Auth.login()
- **TC-FE-014**: 로그인 성공 시 localStorage 저장
  - Given: mock fetch가 200 + TokenResponse 반환
  - When: login("STORE001", 5, "1234")
  - Then: localStorage에 token, storeId, tableId, sessionId 저장
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-FE-015**: 로그인 실패 시 에러 throw
  - Given: mock fetch가 401 반환
  - When: login("STORE001", 5, "0000")
  - Then: ApiError throw
  - Story: US-C01
  - Status: ⬜ Not Started

### Auth.isLoggedIn()
- **TC-FE-016**: 토큰 있으면 true
  - Given: localStorage에 auth_token 존재
  - When: isLoggedIn()
  - Then: true
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-FE-017**: 토큰 없으면 false
  - Given: localStorage에 auth_token 없음
  - When: isLoggedIn()
  - Then: false
  - Story: US-C01
  - Status: ⬜ Not Started

---

## OrderHistoryView 테스트 (order-history.js)

### getStatusBadge()
- **TC-FE-018**: WAITING 상태 뱃지
  - Given: status = "WAITING"
  - When: getStatusBadge("WAITING")
  - Then: {text: "대기중", cssClass: "bg-warning"}
  - Story: US-C07
  - Status: ⬜ Not Started

- **TC-FE-019**: PREPARING 상태 뱃지
  - Given: status = "PREPARING"
  - When: getStatusBadge("PREPARING")
  - Then: {text: "준비중", cssClass: "bg-primary"}
  - Story: US-C07
  - Status: ⬜ Not Started

- **TC-FE-020**: DONE 상태 뱃지
  - Given: status = "DONE"
  - When: getStatusBadge("DONE")
  - Then: {text: "완료", cssClass: "bg-success"}
  - Story: US-C07
  - Status: ⬜ Not Started

---

## Requirements Coverage

| Story | Test Cases | Status |
|-------|------------|--------|
| US-C01 | TC-FE-011, TC-FE-014~017 | ⬜ Pending |
| US-C02 | (UI 렌더링 - 수동 테스트) | ⬜ Pending |
| US-C03 | TC-FE-001, TC-FE-002, TC-FE-008, TC-FE-009 | ⬜ Pending |
| US-C04 | TC-FE-003~007 | ⬜ Pending |
| US-C05 | (주문 생성 - 통합 테스트) | ⬜ Pending |
| US-C06 | (주문 내역 - UI 렌더링) | ⬜ Pending |
| US-C07 | TC-FE-018~020 | ⬜ Pending |
| US-C08 | (무한 스크롤 - 수동 테스트) | ⬜ Pending |
