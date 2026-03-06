# Unit 2: 인증 모듈 - Logical Components

## 컴포넌트 구조 개요

```
com.tableorder.auth/
+-- controller/
|   +-- AuthController              # 인증 API 엔드포인트
+-- service/
|   +-- AuthService                 # 인증 비즈니스 로직
+-- security/
|   +-- SecurityConfig              # Spring Security 설정
|   +-- JwtTokenProvider            # JWT 토큰 생성/검증
|   +-- JwtAuthenticationFilter     # JWT 인증 필터
|   +-- StoreAccessFilter           # 매장 격리 필터
|   +-- CustomAuthenticationEntryPoint  # 401 에러 처리
|   +-- CustomAccessDeniedHandler   # 403 에러 처리
+-- dto/
|   +-- LoginRequest                # 관리자 로그인 요청
|   +-- TableLoginRequest           # 테이블 로그인 요청
|   +-- TokenResponse               # 토큰 응답
|   +-- ChangePasswordRequest       # 비밀번호 변경 요청
|   +-- AuthInfo                    # 인증 정보 (내부)
+-- exception/
    +-- AuthException               # 인증 예외
    +-- GlobalExceptionHandler      # 전역 에러 처리
    +-- ErrorResponse               # 에러 응답 DTO
```

---

## 컴포넌트 상세

### 1. AuthController

**책임**: 인증 관련 REST API 엔드포인트 제공

| 엔드포인트 | 메서드 | 설명 | 인증 |
|-----------|--------|------|------|
| `/api/auth/admin/login` | POST | 관리자 로그인 | 불필요 |
| `/api/auth/table/login` | POST | 테이블 로그인 | 불필요 |
| `/api/auth/admin/password` | PUT | 비밀번호 변경 | ADMIN |
| `/api/auth/validate` | GET | 토큰 유효성 확인 | 모든 역할 |

**의존성**: AuthService

---

### 2. AuthService

**책임**: 인증 비즈니스 로직 처리

| 메서드 | 설명 |
|--------|------|
| adminLogin(LoginRequest) | 매장 확인 → 관리자 조회 → 비밀번호 검증 → 토큰 발급 |
| tableLogin(TableLoginRequest) | 매장 확인 → 테이블 조회 → PIN 검증 → 세션 조회 → 토큰 발급 |
| changePassword(Long adminId, ChangePasswordRequest) | 현재 비밀번호 확인 → 새 비밀번호 해싱 → 저장 → 토큰 무효화 |
| validateToken(String token) | 토큰 검증 → AuthInfo 반환 |

**의존성**: JwtTokenProvider, BCryptPasswordEncoder, StoreRepository, AdminRepository, StoreTableRepository, TableSessionRepository

---

### 3. SecurityConfig

**책임**: Spring Security 필터 체인 및 보안 설정

**설정 내용**:
- CSRF 비활성화 (stateless API)
- 세션 관리: STATELESS
- CORS 설정 (프로필별)
- 필터 체인: JwtAuthenticationFilter → StoreAccessFilter
- URL 패턴별 접근 제어

---

### 4. JwtTokenProvider

**책임**: JWT 토큰 생성, 검증, 파싱

| 메서드 | 설명 |
|--------|------|
| generateToken(AuthInfo) | 페이로드로 JWT 토큰 생성 |
| validateToken(String token) | 서명/만료 검증, AuthInfo 반환 |
| getExpirationTime(String token) | 토큰 만료 시각 조회 |
| shouldRefresh(String token) | 갱신 필요 여부 (잔여 < 8시간) |
| refreshToken(String token) | 동일 페이로드로 새 토큰 생성 |

**설정값** (application.yml):
- `app.jwt.secret`: Secret Key (환경변수 오버라이드)
- `app.jwt.expiration`: 57600 (16시간, 초)
- `app.jwt.refresh-threshold`: 28800 (8시간, 초)

---

### 5. JwtAuthenticationFilter

**책임**: 매 요청마다 JWT 토큰 검증 및 SecurityContext 설정

**처리 흐름**:
1. Authorization 헤더에서 Bearer 토큰 추출
2. 토큰 없으면 → 다음 필터로 (공개 API일 수 있음)
3. JwtTokenProvider.validateToken() 호출
4. role=ADMIN이면 → DB에서 last_token_issued_at 비교
5. SecurityContext에 Authentication 설정
6. 토큰 갱신 필요 시 → X-New-Token 헤더 추가 (ADMIN이면 DB도 갱신)

**상속**: OncePerRequestFilter

---

### 6. StoreAccessFilter

**책임**: URL의 storeId와 토큰의 storeId 일치 검증

**처리 흐름**:
1. URL에서 `/api/stores/{storeId}/` 패턴의 storeId 추출
2. SecurityContext에서 인증 정보의 storeId 추출
3. 불일치 시 → 403 STORE_ACCESS_DENIED
4. `/api/auth/**` 경로는 검증 생략

**상속**: OncePerRequestFilter

---

### 7. CustomAuthenticationEntryPoint

**책임**: 인증 실패 (401) 시 표준 에러 응답 반환

**처리**: AuthenticationException → ErrorResponse JSON 반환

---

### 8. CustomAccessDeniedHandler

**책임**: 접근 거부 (403) 시 표준 에러 응답 반환

**처리**: AccessDeniedException → ErrorResponse JSON 반환

---

### 9. GlobalExceptionHandler

**책임**: 비즈니스 예외를 표준 에러 응답으로 변환

| 예외 | HTTP Status | Error Code |
|------|-------------|------------|
| AuthException | 401 | AUTHENTICATION_FAILED |
| ValidationException | 400 | VALIDATION_FAILED |
| Exception (기타) | 500 | INTERNAL_ERROR |

---

## 컴포넌트 의존성 다이어그램

```
+------------------+
| AuthController   |
+--------+---------+
         |
         v
+------------------+     +--------------------+
| AuthService      |---->| JwtTokenProvider   |
+--------+---------+     +--------------------+
         |                        ^
         v                        |
+------------------+     +--------------------+
| Repositories     |     | JwtAuthFilter      |
| (Store, Admin,   |     +--------------------+
|  Table, Session) |              |
+------------------+              v
                         +--------------------+
                         | StoreAccessFilter  |
                         +--------------------+
                                  |
                                  v
                         +--------------------+
                         | SecurityConfig     |
                         | (FilterChain 조립)  |
                         +--------------------+
```

---

## Admin 엔티티 변경사항 (Unit 1 대비)

Unit 2에서 추가되는 필드:

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| last_token_issued_at | TIMESTAMP | NULLABLE | 마지막 토큰 발급 시각 (단일 세션 검증용) |
