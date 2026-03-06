# Business Logic Model - Unit 4: 고객 프론트엔드

## 1. 앱 초기화 흐름

```
앱 시작 (index.html 로드)
    │
    ▼
localStorage에 인증 정보 존재?
    │
    ├── NO → 로그인 화면 표시
    │
    └── YES → GET /api/auth/validate (저장된 토큰)
                │
                ├── 200 OK → 메뉴 화면 이동
                │
                └── 401 → POST /api/auth/table/login (저장된 credentials)
                            │
                            ├── 200 OK → 토큰 갱신, 메뉴 화면 이동
                            │
                            └── 401 → localStorage 삭제, 로그인 화면 표시
```

---

## 2. 로그인 흐름

```
로그인 폼 제출
    │
    ▼
클라이언트 입력 검증 (BR-VAL-01)
    │
    ├── 실패 → 에러 메시지 표시
    │
    └── 통과 → POST /api/auth/table/login
                │
                ├── 200 OK
                │   ├── localStorage에 인증 정보 저장
                │   │   (storeCode, tableNumber, password,
                │   │    token, storeId, tableId, sessionId)
                │   └── 메뉴 화면으로 전환 (#/menu)
                │
                └── 401 → "로그인 정보가 올바르지 않습니다" 토스트
```

---

## 3. API 공통 처리 흐름 (fetch wrapper)

```
api.request(method, url, body)
    │
    ▼
요청 헤더 구성
    ├── Authorization: Bearer {localStorage.auth_token}
    └── Content-Type: application/json
    │
    ▼
fetch(url, options)
    │
    ▼
응답 수신
    │
    ├── X-New-Token 헤더 존재?
    │   └── YES → localStorage.auth_token 교체 (BR-AUTH-03)
    │
    ├── 200~299 → JSON 파싱 후 반환
    │
    ├── 401 → 자동 재로그인 시도 (BR-AUTH-04)
    │   ├── 성공 → 원래 요청 재시도 (1회)
    │   └── 실패 → 로그인 화면 이동
    │
    └── 기타 에러 → 에러 객체 throw (BR-ERR-01)
```

---

## 4. 메뉴 조회 흐름

```
메뉴 화면 진입
    │
    ▼
GET /api/stores/{storeId}/customer/categories
    │
    ▼
카테고리 탭 렌더링 (가나다순)
    │
    ▼
첫 번째 카테고리 자동 선택
    │
    ▼
GET /api/stores/{storeId}/customer/menus?category={선택된 카테고리}
    │
    ▼
메뉴 카드 그리드 렌더링
    ├── 각 카드: 이미지, 메뉴명, 가격, 설명, 맵기 뱃지
    └── 장바구니 수량 동기화 (BR-CART-06)
        ├── 장바구니에 없음 → [+] 버튼만
        └── 장바구니에 있음 → [-][수량][+] 표시

카테고리 탭 클릭
    │
    ▼
GET /api/stores/{storeId}/customer/menus?category={클릭한 카테고리}
    │
    ▼
메뉴 카드 그리드 갱신
```

---

## 5. 장바구니 관리 흐름

### 5.1 메뉴 추가 (+버튼)

```
메뉴 카드 [+] 클릭
    │
    ▼
localStorage에서 cart_items 로드
    │
    ▼
해당 menuId가 cart_items에 존재?
    │
    ├── YES → quantity += 1
    │
    └── NO → CartItem 생성 {menuId, menuName, unitPrice, quantity:1, hasSpicyOptions}
    │
    ▼
cart_items를 localStorage에 저장
    │
    ▼
UI 갱신
    ├── 메뉴 카드 수량 표시 업데이트
    ├── 장바구니 플로팅 버튼 수량 뱃지 업데이트
    └── 사이드 패널 열려있으면 패널 내용 갱신
```

### 5.2 사이드 패널 조작

```
장바구니 버튼 클릭
    │
    ▼
Bootstrap offcanvas 슬라이드 인 (오른쪽)
    │
    ▼
localStorage에서 cart_items 로드 → 항목 리스트 렌더링
    │
    ├── [+] → quantity += 1 → localStorage 저장 → UI 갱신
    ├── [-] → quantity -= 1
    │         ├── quantity > 0 → localStorage 저장 → UI 갱신
    │         └── quantity == 0 → 항목 제거 → localStorage 저장 → UI 갱신
    ├── [X] → 항목 제거 → localStorage 저장 → UI 갱신
    └── [비우기] → cart_items = [] → localStorage 저장 → UI 갱신
```

---

## 6. 주문 생성 흐름

```
장바구니 패널 "주문하기" 클릭
    │
    ▼
주문 확인 화면 전환 (#/order)
    │
    ▼
cart_items에서 hasSpicyOptions=true인 메뉴 필터링
    │
    ▼
각 메뉴에 대해 GET /menus/{menuId}/spicy-options (병렬 호출)
    │
    ▼
주문 항목 리스트 렌더링
    ├── 메뉴명, 수량 x 단가 = 소계
    ├── 맵기 옵션 드롭다운 (해당 메뉴만)
    └── 요청사항 textarea
    │
    ▼
"주문 확정" 클릭
    │
    ▼
버튼 비활성화 + 로딩 표시 (BR-ORDER-07)
    │
    ▼
CartItem[] → OrderRequest 변환 (BR-ORDER-04)
    │
    ▼
POST /api/stores/{storeId}/customer/orders
    │
    ├── 201 Created
    │   ├── 주문 완료 토스트 (주문번호: {orderNumber})
    │   ├── cart_items = [] → localStorage 저장
    │   ├── auth_sessionId = response.sessionId → localStorage 저장
    │   └── 5초 후 메뉴 화면 이동 (#/menu)
    │
    ├── 409 PRICE_MISMATCH
    │   ├── "메뉴 가격이 변경되었습니다" 토스트
    │   └── 장바구니 유지 (사용자가 직접 확인)
    │
    └── 기타 에러 → 에러 토스트 + 장바구니 유지
    │
    ▼
버튼 활성화 복원
```

---

## 7. 주문 내역 조회 흐름

```
주문내역 탭 클릭
    │
    ▼
currentPage = 0, hasMore = true
    │
    ▼
GET /api/stores/{storeId}/customer/orders?page=0&size=10
    │
    ├── orders 비어있음 → "주문 내역이 없습니다" 메시지
    │
    └── orders 있음 → 주문 카드 리스트 렌더링
        ├── 각 카드: 주문번호, 시각, 상태 뱃지, 총 금액
        ├── 카드 클릭 → 상세 항목 토글 (아코디언)
        └── hasMore = (page < totalPages - 1)

스크롤 하단 도달 (Intersection Observer)
    │
    ├── hasMore == false → 무시
    ├── isLoading == true → 무시 (중복 방지)
    │
    └── hasMore == true
        ├── isLoading = true
        ├── currentPage += 1
        ├── GET /orders?page={currentPage}&size=10
        ├── 응답 카드를 리스트에 추가
        ├── hasMore 갱신
        └── isLoading = false
```

---

## 8. API 연동 매핑

| 화면 | API | 호출 시점 |
|------|-----|-----------|
| 로그인 | `POST /api/auth/table/login` | 로그인 폼 제출 / 자동 로그인 |
| 로그인 | `GET /api/auth/validate` | 앱 초기화 시 토큰 검증 |
| 메뉴 | `GET /categories` | 메뉴 화면 진입 |
| 메뉴 | `GET /menus?category=` | 카테고리 선택 |
| 주문 확인 | `GET /menus/{id}/spicy-options` | 주문 확인 화면 진입 |
| 주문 확인 | `POST /orders` | 주문 확정 버튼 클릭 |
| 주문 내역 | `GET /orders?page=&size=` | 주문내역 화면 진입 / 스크롤 |

---

## 9. 상태 관리 요약

| 상태 | 저장소 | 갱신 시점 |
|------|--------|-----------|
| 인증 정보 | localStorage | 로그인 성공, 토큰 갱신 |
| 장바구니 | localStorage | 메뉴 추가/수량변경/삭제, 주문 완료 |
| 현재 카테고리 | JS 변수 | 카테고리 탭 클릭 |
| 메뉴 목록 | JS 변수 | 카테고리 변경 시 API 응답 |
| 주문 내역 | JS 변수 | 주문내역 화면 진입/스크롤 |
| 현재 화면 | URL hash | 화면 전환 시 |
