# NFR Requirements - Unit 4: 고객 프론트엔드

## 1. 성능 요구사항 (Performance)

| ID | 요구사항 | 목표값 | 비고 |
|----|----------|--------|------|
| NFR-PERF-01 | 초기 페이지 로드 시간 | < 3초 (LAN 환경) | HTML + CSS + JS + Bootstrap CDN |
| NFR-PERF-02 | 화면 전환 시간 | < 200ms | SPA hash 라우팅, DOM 조작 |
| NFR-PERF-03 | API 응답 후 UI 갱신 | < 100ms | fetch 응답 → DOM 렌더링 |
| NFR-PERF-04 | 장바구니 조작 반응 | 즉시 (< 50ms) | localStorage 동기 처리 |

### 최적화 전략
- Bootstrap 5 CDN minified 버전 사용
- 이미지 lazy loading 미적용 (메뉴 이미지 수 제한적)
- API 호출 최소화: 카테고리 변경 시에만 메뉴 재조회

---

## 2. 가용성 요구사항 (Availability)

| ID | 요구사항 | 목표값 | 비고 |
|----|----------|--------|------|
| NFR-AVAIL-01 | 정적 파일 가용성 | Backend 서버와 동일 | Spring Boot static/ 배포 |
| NFR-AVAIL-02 | 네트워크 오류 처리 | 기본 에러 메시지 표시 | 재시도 로직 미적용 |
| NFR-AVAIL-03 | 장바구니 데이터 보존 | 브라우저 새로고침 시 유지 | localStorage 기반 |

### 네트워크 오류 대응
- fetch 실패 시 토스트 알림: "네트워크 연결을 확인해주세요"
- 타임아웃 10초 설정
- 오프라인 감지/Service Worker 미적용 (기본 수준)

---

## 3. 보안 요구사항 (Security)

| ID | 요구사항 | 구현 방식 | 비고 |
|----|----------|-----------|------|
| NFR-SEC-01 | JWT 토큰 저장 | localStorage | XSS 방어는 입력 sanitize로 대응 |
| NFR-SEC-02 | 토큰 자동 갱신 | X-New-Token 헤더 감지 | 공통 fetch wrapper |
| NFR-SEC-03 | 인증 실패 처리 | 401 시 자동 재로그인 시도 | 실패 시 로그인 화면 이동 |
| NFR-SEC-04 | XSS 방지 | innerHTML 사용 최소화, textContent 우선 | 사용자 입력 표시 시 escape |
| NFR-SEC-05 | CORS | Backend CORS 설정에 의존 | 동일 서버 배포로 CORS 이슈 없음 |

---

## 4. 호환성 요구사항 (Compatibility)

| ID | 요구사항 | 목표 | 비고 |
|----|----------|------|------|
| NFR-COMPAT-01 | 브라우저 지원 | Chrome, Safari, Firefox, Edge (최신 버전) | ES6+ 문법 사용 가능 |
| NFR-COMPAT-02 | 반응형 디자인 | 모바일(360px) ~ 태블릿(1024px) | Bootstrap 5 그리드 |
| NFR-COMPAT-03 | 태블릿 최적화 | 터치 인터랙션 고려 | 버튼 크기 최소 44x44px |

---

## 5. 접근성 요구사항 (Accessibility)

| ID | 요구사항 | 수준 | 비고 |
|----|----------|------|------|
| NFR-A11Y-01 | 시맨틱 HTML | 기본 | header, nav, main, section 등 |
| NFR-A11Y-02 | 이미지 alt 속성 | 기본 | 메뉴 이미지에 메뉴명 alt |
| NFR-A11Y-03 | 폼 label 연결 | 기본 | 로그인 폼 input-label 연결 |
| NFR-A11Y-04 | 색상 대비 | Bootstrap 기본값 | 추가 커스텀 불필요 |

---

## 6. 유지보수성 요구사항 (Maintainability)

| ID | 요구사항 | 구현 방식 | 비고 |
|----|----------|-----------|------|
| NFR-MAINT-01 | 모듈 분리 | 화면별 JS 파일 분리 | app.js, api.js, auth.js, menu.js, cart.js, order.js, order-history.js |
| NFR-MAINT-02 | API 엔드포인트 관리 | api.js에 BASE_URL 상수화 | 환경별 변경 용이 |
| NFR-MAINT-03 | 코드 주석 | 주요 함수에 JSDoc 스타일 주석 | 비즈니스 로직 설명 |
