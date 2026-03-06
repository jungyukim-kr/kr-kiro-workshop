# Tech Stack Decisions - Unit 4: 고객 프론트엔드

## 기술 스택 결정

| 카테고리 | 기술 | 버전 | 결정 근거 |
|----------|------|------|-----------|
| 언어 | Vanilla JavaScript (ES6+) | - | 프레임워크 없이 경량 구현, 학습 곡선 없음 |
| 마크업 | HTML5 | - | 시맨틱 태그 활용 |
| 스타일 | CSS3 + Bootstrap 5 | 5.3.x | 반응형 그리드, 컴포넌트 (offcanvas, toast, navbar) |
| Bootstrap 로드 | CDN | - | 빌드 도구 불필요, 즉시 사용 |
| 배포 | Spring Boot static/ | - | Backend와 동일 서버, 별도 웹서버 불필요 |
| 상태 관리 | localStorage | - | 장바구니, 인증 정보 영속 저장 |
| 라우팅 | Hash-based (#/) | - | SPA 스타일 화면 전환, 서버 설정 불필요 |
| HTTP 클라이언트 | Fetch API | - | 브라우저 내장, 추가 라이브러리 불필요 |

---

## 외부 의존성

| 의존성 | CDN URL | 용도 |
|--------|---------|------|
| Bootstrap 5 CSS | `https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css` | 스타일, 그리드, 컴포넌트 |
| Bootstrap 5 JS | `https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js` | Offcanvas, Toast, Collapse 등 |

- 추가 라이브러리 없음 (jQuery, Axios 등 미사용)
- 아이콘: Bootstrap Icons CDN 또는 유니코드 이모지 사용

---

## 배포 구조

```
backend/src/main/resources/static/
├── customer/
│   ├── index.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── app.js
│       ├── api.js
│       ├── auth.js
│       ├── menu.js
│       ├── cart.js
│       ├── order.js
│       └── order-history.js
```

- 접근 URL: `http://localhost:8080/customer/index.html`
- Spring Boot의 정적 리소스 서빙 기능 활용
- 별도 빌드 프로세스 불필요 (번들링, 트랜스파일 없음)

---

## 브라우저 호환성 매트릭스

| 브라우저 | 최소 버전 | ES6+ | Fetch API | localStorage | Bootstrap 5 |
|----------|-----------|------|-----------|-------------|-------------|
| Chrome | 80+ | O | O | O | O |
| Safari | 14+ | O | O | O | O |
| Firefox | 80+ | O | O | O | O |
| Edge | 80+ | O | O | O | O |

---

## 기각된 대안

| 대안 | 기각 사유 |
|------|-----------|
| React/Vue/Angular | 프레임워크 오버헤드, 빌드 도구 필요, 학습 곡선 |
| Tailwind CSS | 빌드 프로세스 필요 (purge), CDN 용량 큼 |
| Axios | Fetch API로 충분, 추가 의존성 불필요 |
| Nginx 별도 배포 | 인프라 복잡도 증가, 워크숍 범위 초과 |
| Service Worker | 오프라인 요구사항 없음, 복잡도 대비 효용 낮음 |
