# NFR Design Patterns - Unit 4: 고객 프론트엔드

## 1. API 통신 패턴: 공통 Fetch Wrapper

### 목적
모든 API 호출에 대한 인증, 토큰 갱신, 에러 처리를 중앙화합니다.

### 패턴 구조

```
api.request(method, endpoint, body)
    │
    ├── 1. Authorization 헤더 자동 주입
    ├── 2. fetch 호출 (timeout 10초)
    ├── 3. X-New-Token 헤더 감지 → 토큰 자동 교체
    ├── 4. 401 응답 → 자동 재로그인 시도 (1회)
    └── 5. 에러 응답 → 표준 에러 객체 throw
```

### 적용 NFR
- NFR-SEC-02 (토큰 자동 갱신)
- NFR-SEC-03 (인증 실패 처리)
- NFR-MAINT-02 (API 엔드포인트 관리)

---

## 2. 상태 영속화 패턴: localStorage Wrapper

### 목적
장바구니와 인증 정보의 영속 저장을 일관된 인터페이스로 관리합니다.

### 패턴 구조

```
storage.get(key)     → JSON.parse(localStorage.getItem(key))
storage.set(key, val) → localStorage.setItem(key, JSON.stringify(val))
storage.remove(key)   → localStorage.removeItem(key)
```

### 적용 NFR
- NFR-AVAIL-03 (장바구니 데이터 보존)
- NFR-PERF-04 (장바구니 조작 즉시 반응)

---

## 3. 화면 전환 패턴: Hash Router

### 목적
SPA 스타일 화면 전환을 서버 설정 없이 구현합니다.

### 패턴 구조

```
window.onhashchange → router.navigate(hash)
    │
    ├── #/login    → LoginView.render()
    ├── #/menu     → MenuView.render()
    ├── #/order    → OrderConfirmView.render()
    └── #/history  → OrderHistoryView.render()

각 View:
    1. 이전 View 컨테이너 숨김 (display:none)
    2. 현재 View 컨테이너 표시 (display:block)
    3. 필요 시 API 호출 → DOM 렌더링
```

### 적용 NFR
- NFR-PERF-02 (화면 전환 <200ms)

---

## 4. XSS 방지 패턴: Safe Rendering

### 목적
사용자 입력 및 서버 데이터를 안전하게 DOM에 렌더링합니다.

### 패턴 규칙

| 상황 | 방법 | 예시 |
|------|------|------|
| 텍스트 표시 | `textContent` 사용 | `el.textContent = menuName` |
| HTML 구조 생성 | `createElement` + `textContent` | DOM API 조합 |
| innerHTML 필요 시 | 데이터 부분만 escape | `escapeHtml(userInput)` |

### escapeHtml 함수

```
escapeHtml(str):
    & → &amp;
    < → &lt;
    > → &gt;
    " → &quot;
    ' → &#039;
```

### 적용 NFR
- NFR-SEC-04 (XSS 방지)

---

## 5. 무한 스크롤 패턴: Intersection Observer

### 목적
주문 내역 페이지네이션을 스크롤 기반으로 구현합니다.

### 패턴 구조

```
sentinel 요소 (리스트 하단에 빈 div)
    │
    ▼
IntersectionObserver 감시
    │
    ├── sentinel이 뷰포트 진입
    │   ├── isLoading == true → 무시
    │   ├── hasMore == false → 무시
    │   └── 조건 충족 → 다음 페이지 로드
    │       ├── isLoading = true
    │       ├── GET /orders?page={next}&size=10
    │       ├── 응답 카드 추가 렌더링
    │       ├── hasMore 갱신
    │       └── isLoading = false
    │
    └── sentinel이 뷰포트 밖 → 대기
```

### 적용 NFR
- NFR-PERF-03 (API 응답 후 UI 갱신 <100ms)

---

## 6. 반응형 디자인 패턴: Bootstrap Grid

### 목적
모바일~태블릿 화면 크기에 자동 적응합니다.

### 브레이크포인트 활용

| 화면 크기 | Bootstrap 클래스 | 메뉴 카드 열 수 |
|-----------|-----------------|----------------|
| < 576px (모바일) | col-6 | 2열 |
| 576~768px | col-sm-4 | 3열 |
| 768~1024px (태블릿) | col-md-3 | 4열 |

### 적용 NFR
- NFR-COMPAT-02 (반응형 디자인)
- NFR-COMPAT-03 (태블릿 최적화)
