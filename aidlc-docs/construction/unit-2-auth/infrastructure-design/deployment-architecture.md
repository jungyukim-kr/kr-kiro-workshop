# Unit 2: 인증 모듈 - Deployment Architecture

## 배포 아키텍처 다이어그램

```
+--------------------------------------------------+
|              Docker Compose                       |
|                                                   |
|  +--------------------------------------------+  |
|  |  Spring Boot App (port 8080)               |  |
|  |                                            |  |
|  |  +-- SecurityFilterChain                   |  |
|  |  |   +-- CorsFilter                        |  |
|  |  |   +-- JwtAuthenticationFilter           |  |
|  |  |   +-- StoreAccessFilter                 |  |
|  |  |   +-- AuthorizationFilter               |  |
|  |  |                                         |  |
|  |  +-- /api/auth/** (AuthController)         |  |
|  |  +-- /api/stores/** (향후 Unit 3,5)        |  |
|  |                                            |  |
|  |  ENV: JWT_SECRET, SPRING_PROFILES_ACTIVE   |  |
|  +--------------------------------------------+  |
|           |                                       |
|           v                                       |
|  +--------------------------------------------+  |
|  |  PostgreSQL (port 5432)                    |  |
|  |                                            |  |
|  |  Tables: store, admin, store_table,        |  |
|  |          table_session, ...                |  |
|  +--------------------------------------------+  |
|                                                   |
+--------------------------------------------------+
```

## 인증 요청 흐름

### 관리자 로그인
```
[Admin Browser]
    |
    | POST /api/auth/admin/login
    | {storeCode, username, password}
    |
    v
[Spring Boot - AuthController]
    |
    v
[AuthService]
    |-- Store 조회 (PostgreSQL)
    |-- Admin 조회 (PostgreSQL)
    |-- bcrypt 비밀번호 검증
    |-- last_token_issued_at 갱신 (PostgreSQL)
    |-- JWT 토큰 생성 (jjwt)
    |
    v
[Response: {token, storeId, role}]
    |
    v
[Admin Browser - localStorage에 토큰 저장]
```

### 테이블 로그인
```
[Table Tablet Browser]
    |
    | POST /api/auth/table/login
    | {storeCode, tableNumber, password}
    |
    v
[Spring Boot - AuthController]
    |
    v
[AuthService]
    |-- Store 조회 (PostgreSQL)
    |-- StoreTable 조회 (PostgreSQL)
    |-- bcrypt PIN 검증
    |-- 활성 세션 조회 (PostgreSQL)
    |-- JWT 토큰 생성 (jjwt)
    |
    v
[Response: {token, storeId, tableId, sessionId}]
    |
    v
[Tablet Browser - localStorage에 토큰 저장]
```

### 보호된 API 요청
```
[Client]
    |
    | GET /api/stores/1/admin/orders
    | Authorization: Bearer {token}
    |
    v
[JwtAuthenticationFilter]
    |-- 토큰 추출 및 검증 (jjwt)
    |-- ADMIN이면: last_token_issued_at 비교 (PostgreSQL)
    |-- SecurityContext 설정
    |-- 토큰 갱신 필요 시: X-New-Token 헤더 추가
    |
    v
[StoreAccessFilter]
    |-- URL storeId(1) == 토큰 storeId(1) 확인
    |
    v
[AuthorizationFilter]
    |-- ADMIN 역할 확인
    |
    v
[Controller - 비즈니스 로직 처리]
```

## 환경변수 목록

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| JWT_SECRET | JWT 서명 키 | default-dev-secret... | 운영 시 필수 |
| SPRING_PROFILES_ACTIVE | 활성 프로필 | (없음=기본) | 선택 |
| DB_HOST | PostgreSQL 호스트 | localhost | Docker 시 |
| DB_PORT | PostgreSQL 포트 | 5432 | 선택 |
| DB_NAME | 데이터베이스명 | tableorder | 선택 |
| DB_USERNAME | DB 사용자 | tableorder | 선택 |
| DB_PASSWORD | DB 비밀번호 | tableorder | 운영 시 필수 |
