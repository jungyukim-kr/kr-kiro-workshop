# Frontend Components - Unit 4: 고객 프론트엔드

## 기술 결정 요약

| 항목 | 결정 |
|------|------|
| 레이아웃 | SPA 스타일 (단일 HTML, JS로 화면 전환) |
| 네비게이션 | 상단 헤더에 네비게이션 포함 |
| 장바구니 UI | 사이드 패널 (슬라이드 인/아웃) |
| 메뉴 추가 | 메뉴 카드에 +/- 버튼 직접 표시 |
| 맵기/요청사항 | 주문 확인 화면에서 인라인 표시 |
| CSS 프레임워크 | Bootstrap 5 CDN |
| 반응형 | 모바일~태블릿 반응형 |
| 토큰 관리 | 공통 fetch wrapper로 X-New-Token 자동 교체 |

---

## 파일 구조

```
frontend/customer/
├── index.html              # 단일 HTML (SPA 진입점)
├── css/
│   └── style.css           # 커스텀 스타일 (Bootstrap 보완)
├── js/
│   ├── app.js              # 앱 초기화, 라우터, 화면 전환
│   ├── api.js              # 공통 fetch wrapper (JWT, X-New-Token 처리)
│   ├── auth.js             # 로그인/자동 로그인 로직
│   ├── menu.js             # 카테고리/메뉴 조회 화면
│   ├── cart.js             # 장바구니 (사이드 패널 + localStorage)
│   ├── order.js            # 주문 확인/생성 화면
│   └── order-history.js    # 주문 내역 조회 화면
└── assets/
    └── (이미지 등 정적 리소스)
```

---

## 화면 구성 (Views)

### 1. 로그인 화면 (LoginView)

| 항목 | 설명 |
|------|------|
| 표시 조건 | localStorage에 로그인 정보 없거나 자동 로그인 실패 시 |
| Stories | US-C01 |

**UI 요소:**
- 매장 식별자 입력 (text input)
- 테이블 번호 입력 (number input)
- 4자리 PIN 입력 (password input, maxlength=4)
- 로그인 버튼
- 에러 메시지 영역

**동작:**
1. 입력값 검증 후 `POST /api/auth/table/login` 호출
2. 성공 시 localStorage에 `storeCode`, `tableNumber`, `password`, `token`, `storeId` 저장
3. 메뉴 화면으로 전환

### 2. 메뉴 화면 (MenuView)

| 항목 | 설명 |
|------|------|
| 표시 조건 | 로그인 완료 후 기본 화면 |
| Stories | US-C02, US-C03 |

**UI 요소:**
- 상단 헤더: 매장명/테이블 번호 + 네비게이션 (메뉴/주문내역)
- 카테고리 탭 바 (가로 스크롤, Bootstrap nav-pills)
- 메뉴 카드 그리드 (Bootstrap row/col)
  - 각 카드: 이미지, 메뉴명, 가격, 설명, 맵기 수준 뱃지
  - +/- 버튼 + 현재 수량 표시 (장바구니에 있는 경우)
- 장바구니 플로팅 버튼 (총 수량 뱃지, 클릭 시 사이드 패널 열기)

**동작:**
1. 화면 진입 시 `GET /categories` → 카테고리 목록 로드
2. 첫 번째 카테고리 자동 선택 → `GET /menus?category=` → 메뉴 목록 로드
3. 카테고리 탭 클릭 시 해당 카테고리 메뉴 로드
4. +버튼: 장바구니에 수량 1 추가 (localStorage 즉시 반영)
5. -버튼: 수량 1 감소, 0이면 장바구니에서 제거

### 3. 장바구니 사이드 패널 (CartPanel)

| 항목 | 설명 |
|------|------|
| 표시 조건 | 장바구니 버튼 클릭 시 슬라이드 인 |
| Stories | US-C03, US-C04 |

**UI 요소:**
- 패널 헤더: "장바구니" + 닫기 버튼
- 장바구니 항목 리스트
  - 각 항목: 메뉴명, 단가, +/- 수량 조절, 삭제(X) 버튼
- 장바구니 비우기 버튼
- 하단 고정: 총 금액 + "주문하기" 버튼
- 빈 장바구니: "장바구니가 비어있습니다" 메시지

**동작:**
1. 오른쪽에서 슬라이드 인 (Bootstrap offcanvas)
2. +/- 버튼으로 수량 조절, 수량 0이면 자동 제거
3. 삭제(X) 버튼으로 개별 항목 제거
4. "장바구니 비우기"로 전체 삭제
5. "주문하기" 클릭 시 주문 확인 화면으로 전환
6. 모든 변경 즉시 localStorage 반영

### 4. 주문 확인 화면 (OrderConfirmView)

| 항목 | 설명 |
|------|------|
| 표시 조건 | 장바구니에서 "주문하기" 클릭 시 |
| Stories | US-C05 |

**UI 요소:**
- 상단: "주문 확인" 제목 + 뒤로가기 버튼
- 주문 항목 리스트 (각 항목 아래 인라인):
  - 메뉴명, 수량, 단가, 소계
  - 맵기 옵션 드롭다운 (hasSpicyOptions=true인 메뉴만)
  - 요청사항 텍스트 입력 (textarea)
- 하단 고정: 총 금액 + "주문 확정" 버튼
- 빈 장바구니면 "주문 확정" 버튼 비활성화

**동작:**
1. 화면 진입 시 hasSpicyOptions=true인 메뉴에 대해 `GET /menus/{menuId}/spicy-options` 호출
2. 맵기 옵션 드롭다운에 옵션 목록 표시 (기본: 선택 안 함)
3. "주문 확정" 클릭 → `POST /orders` 호출
4. 성공 시: 주문 완료 알림 (주문 번호 표시), 장바구니 비우기, 5초 후 메뉴 화면 이동
5. 실패 시: 에러 메시지 표시, 장바구니 유지
6. PRICE_MISMATCH 에러 시: "메뉴 가격이 변경되었습니다" 알림 + 장바구니 가격 갱신 안내

### 5. 주문 내역 화면 (OrderHistoryView)

| 항목 | 설명 |
|------|------|
| 표시 조건 | 상단 헤더 "주문내역" 탭 클릭 시 |
| Stories | US-C06, US-C07, US-C08 |

**UI 요소:**
- 주문 카드 리스트 (최신순)
  - 각 카드: 주문 번호, 주문 시각, 상태 뱃지, 총 금액
  - 펼치기/접기: 주문 항목 상세 (메뉴명, 수량, 단가, 맵기옵션, 요청사항)
- 상태 뱃지 색상: WAITING(노란색), PREPARING(파란색), DONE(초록색)
- 주문 없음: "주문 내역이 없습니다" 메시지
- 무한 스크롤 (page 파라미터 증가)

**동작:**
1. 화면 진입 시 `GET /orders?page=0&size=10` 호출
2. 스크롤 하단 도달 시 다음 페이지 로드 (totalPages 초과 시 중단)
3. 주문 카드 클릭 시 상세 항목 토글

---

## 공통 컴포넌트

### 상단 헤더 (Header)

```
┌─────────────────────────────────────────────┐
│  🍽️ [매장명] - 테이블 [N]    [메뉴] [주문내역] │
└─────────────────────────────────────────────┘
```

- Bootstrap navbar 사용
- 현재 활성 탭 하이라이트
- 장바구니 아이콘 + 수량 뱃지 (우측)

### 토스트 알림 (Toast)

- Bootstrap Toast 컴포넌트 활용
- 주문 성공/실패, 에러 메시지 표시
- 3초 후 자동 사라짐

### 로딩 스피너 (Spinner)

- API 호출 중 표시
- Bootstrap spinner 사용

---

## 화면 전환 (Router)

SPA 스타일로 단일 HTML 내에서 JS로 화면 전환:

```
hash 기반 라우팅:
  #/login     → LoginView
  #/menu      → MenuView (기본)
  #/order     → OrderConfirmView
  #/history   → OrderHistoryView
```

- `window.onhashchange` 이벤트로 화면 전환
- 각 View는 `<div id="view-{name}">` 컨테이너에 렌더링
- 화면 전환 시 이전 View 숨기고 새 View 표시
