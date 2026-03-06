# Infrastructure Design - Unit 4: 고객 프론트엔드

## 인프라 변경사항

Unit 4는 정적 파일(HTML/CSS/JS)로 구성되며, 기존 Unit 1 인프라(Spring Boot + Docker Compose)에 추가 변경 없이 배포됩니다.

---

## 배포 방식

### Spring Boot Static Resources

정적 파일을 Spring Boot의 기본 정적 리소스 경로에 배치합니다.

```
backend/src/main/resources/static/customer/
├── index.html
├── css/
│   └── style.css
└── js/
    ├── app.js
    ├── api.js
    ├── auth.js
    ├── menu.js
    ├── cart.js
    ├── order.js
    └── order-history.js
```

- 접근 URL: `http://localhost:8080/customer/index.html`
- Spring Boot가 `static/` 디렉토리를 자동으로 서빙
- 별도 웹서버(Nginx 등) 불필요
- 별도 빌드 프로세스 불필요

---

## 개발 환경 구성

### 개발 시 파일 위치

개발 편의를 위해 `frontend/customer/` 디렉토리에서 작업하고, 빌드/배포 시 `backend/src/main/resources/static/customer/`로 복사하는 방식을 사용합니다.

```
프로젝트 루트/
├── frontend/
│   └── customer/          ← 개발 작업 디렉토리
│       ├── index.html
│       ├── css/style.css
│       └── js/*.js
│
└── backend/
    └── src/main/resources/
        └── static/
            └── customer/  ← 배포 디렉토리 (frontend/customer/ 복사)
```

### 개발 워크플로우

1. `frontend/customer/`에서 파일 편집
2. 브라우저에서 직접 `index.html` 열어 확인 (API 호출은 CORS로 인해 Backend 실행 필요)
3. 배포 시 `frontend/customer/` → `backend/src/main/resources/static/customer/` 복사
4. `./gradlew bootRun`으로 통합 테스트

---

## 기존 인프라 영향

| 항목 | 영향 | 비고 |
|------|------|------|
| Docker Compose | 변경 없음 | 기존 구성 그대로 사용 |
| Spring Boot | 변경 없음 | static/ 자동 서빙 |
| PostgreSQL | 변경 없음 | Frontend에서 직접 접근 안 함 |
| CORS 설정 | 변경 없음 | 동일 서버 배포로 CORS 이슈 없음 |
| Security Filter | 변경 없음 | 정적 파일 경로는 permitAll 설정 필요 |

### Security 설정 추가 필요

Spring Security에서 정적 파일 경로를 인증 없이 접근 가능하도록 설정:

```
/customer/** → permitAll (정적 파일)
```

이 설정은 기존 SecurityConfig에 추가합니다.

---

## 외부 CDN 의존

| 리소스 | CDN | 비고 |
|--------|-----|------|
| Bootstrap 5 CSS | jsdelivr.net | 인터넷 연결 필요 |
| Bootstrap 5 JS | jsdelivr.net | 인터넷 연결 필요 |

- CDN 장애 시 UI 스타일이 깨질 수 있음
- 오프라인 환경 필요 시 로컬 파일로 대체 가능
