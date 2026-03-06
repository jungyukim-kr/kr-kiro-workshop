# Logical Components - Unit 4: 고객 프론트엔드

## 모듈 구조

```
frontend/customer/
│
├── index.html          ← 단일 진입점 (SPA)
│   ├── Bootstrap 5 CDN (CSS + JS)
│   ├── View 컨테이너 (div#view-login, div#view-menu, ...)
│   └── 공통 UI (header, toast, spinner, offcanvas)
│
├── css/style.css       ← 커스텀 스타일
│
└── js/
    ├── app.js          ← 앱 초기화 + Hash Router
    ├── api.js          ← 공통 Fetch Wrapper
    ├── auth.js         ← 로그인/자동 로그인
    ├── menu.js         ← 카테고리/메뉴 조회
    ├── cart.js         ← 장바구니 (localStorage)
    ├── order.js        ← 주문 확인/생성
    └── order-history.js ← 주문 내역 (무한 스크롤)
```

---

## 컴포넌트별 책임

### app.js (앱 코어)
| 책임 | 설명 |
|------|------|
| 초기화 | DOMContentLoaded 시 앱 부트스트랩 |
| 라우팅 | hash 변경 감지 → View 전환 |
| 인증 체크 | 앱 시작 시 자동 로그인 시도 |

### api.js (API 통신)
| 책임 | 설명 |
|------|------|
| BASE_URL 관리 | API 엔드포인트 상수 |
| 인증 헤더 | Authorization Bearer 자동 주입 |
| 토큰 갱신 | X-New-Token 감지 → localStorage 교체 |
| 에러 처리 | 401 재로그인, 표준 에러 throw |
| 타임아웃 | AbortController 10초 |

### auth.js (인증)
| 책임 | 설명 |
|------|------|
| 로그인 폼 | 입력 검증 + API 호출 |
| 자동 로그인 | localStorage credentials → validate → login |
| 로그아웃 | localStorage 삭제 → 로그인 화면 |

### menu.js (메뉴)
| 책임 | 설명 |
|------|------|
| 카테고리 로드 | GET /categories → 탭 렌더링 |
| 메뉴 로드 | GET /menus?category= → 카드 그리드 |
| 장바구니 연동 | 카드에 +/- 버튼 + 수량 동기화 |

### cart.js (장바구니)
| 책임 | 설명 |
|------|------|
| CRUD | 추가/수량변경/삭제/비우기 |
| 영속화 | localStorage 즉시 반영 |
| 사이드 패널 | Bootstrap offcanvas 렌더링 |
| 총 금액 | Σ(unitPrice × quantity) 계산 |

### order.js (주문)
| 책임 | 설명 |
|------|------|
| 주문 확인 | 장바구니 → 주문 항목 리스트 |
| 맵기 옵션 | GET /spicy-options → 드롭다운 |
| 요청사항 | textarea 입력 |
| 주문 생성 | POST /orders + 성공/실패 처리 |

### order-history.js (주문 내역)
| 책임 | 설명 |
|------|------|
| 주문 조회 | GET /orders?page=&size= |
| 무한 스크롤 | IntersectionObserver |
| 상태 뱃지 | WAITING/PREPARING/DONE 색상 |
| 상세 토글 | 아코디언 펼치기/접기 |

---

## 컴포넌트 의존 관계

```
app.js
  ├── api.js (모든 모듈이 의존)
  ├── auth.js → api.js
  ├── menu.js → api.js, cart.js
  ├── cart.js (독립, localStorage만 의존)
  ├── order.js → api.js, cart.js
  └── order-history.js → api.js
```

---

## 외부 의존

| 컴포넌트 | 의존 대상 | 통신 방식 |
|----------|-----------|-----------|
| auth.js | Unit 2 Auth API | REST (POST /auth/table/login, GET /auth/validate) |
| menu.js | Unit 3 Customer API | REST (GET /categories, GET /menus) |
| order.js | Unit 3 Customer API | REST (GET /spicy-options, POST /orders) |
| order-history.js | Unit 3 Customer API | REST (GET /orders) |
| 모든 모듈 | Unit 2 JwtAuthenticationFilter | X-New-Token 헤더 (자동 갱신) |
