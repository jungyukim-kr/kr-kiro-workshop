# Contract/Interface Definition for Unit 2: 인증 모듈

## Unit Context
- **Stories**: US-A01 (관리자 로그인), US-A03 (비밀번호 변경), US-C01 (테이블 로그인)
- **Dependencies**: Unit 1 (Store, Admin, StoreTable, TableSession 엔티티, schema.sql, build.gradle)
- **Database Entities**: Admin (수정: last_token_issued_at 추가), Store/StoreTable/TableSession (참조만)

## Unit 1 기존 코드 수정 범위 (중복 제외)
- `schema.sql`: admin 테이블에 `last_token_issued_at TIMESTAMP` 컬럼 추가
- `application.yml`: `app.jwt.refresh-threshold` 설정 추가
- `Admin.java`: `lastTokenIssuedAt` 필드 추가
- `SecurityConfig.java`: Unit 2 필터 체인으로 교체 (auth 패키지로 이동)

---

## Repository Layer

### AdminRepository
- `findByStoreIdAndUsername(Long storeId, String username) -> Optional<Admin>`: 매장ID+사용자명으로 관리자 조회
- `findById(Long id) -> Optional<Admin>`: ID로 관리자 조회

### StoreRepository
- `findByStoreCode(String storeCode) -> Optional<Store>`: 매장 코드로 매장 조회

### StoreTableRepository
- `findByStoreIdAndTableNumber(Long storeId, Integer tableNumber) -> Optional<StoreTable>`: 매장ID+테이블번호로 테이블 조회

### TableSessionRepository
- `findByTableIdAndActiveTrue(Long tableId) -> Optional<TableSession>`: 테이블의 활성 세션 조회

---

## Business Logic Layer

### AuthService
- `adminLogin(LoginRequest request) -> TokenResponse`: 관리자 로그인
  - Args: LoginRequest (storeCode, username, password)
  - Returns: TokenResponse (token, storeId, role, userId, tableId, sessionId)
  - Raises: AuthException (인증 실패)
- `tableLogin(TableLoginRequest request) -> TokenResponse`: 테이블 로그인
  - Args: TableLoginRequest (storeCode, tableNumber, password)
  - Returns: TokenResponse
  - Raises: AuthException (인증 실패)
- `changePassword(Long adminId, ChangePasswordRequest request) -> void`: 비밀번호 변경
  - Args: adminId (JWT에서 추출), ChangePasswordRequest (currentPassword, newPassword)
  - Returns: void
  - Raises: AuthException (현재 비밀번호 불일치), IllegalArgumentException (관리자 미존재)
- `validateToken() -> ValidateResponse`: 토큰 유효성 확인 (SecurityContext에서 정보 추출)
  - Returns: ValidateResponse (valid, role, storeId, userId, tableId, sessionId)

### JwtTokenProvider
- `generateToken(AuthInfo authInfo) -> String`: JWT 토큰 생성
  - Args: AuthInfo (userId, storeId, role, tableId, sessionId)
  - Returns: JWT 토큰 문자열
- `validateToken(String token) -> AuthInfo`: 토큰 검증 및 파싱
  - Args: JWT 토큰 문자열
  - Returns: AuthInfo
  - Raises: ExpiredJwtException, JwtException
- `shouldRefresh(String token) -> boolean`: 갱신 필요 여부 확인
  - Returns: 잔여시간 < refresh-threshold이면 true
- `refreshToken(String token) -> String`: 동일 페이로드로 새 토큰 생성

---

## API Layer

### AuthController
- `POST /api/auth/admin/login`: 관리자 로그인 → TokenResponse
- `POST /api/auth/table/login`: 테이블 로그인 → TokenResponse
- `PUT /api/auth/admin/password`: 비밀번호 변경 → MessageResponse
- `GET /api/auth/validate`: 토큰 유효성 확인 → ValidateResponse

---

## Security Filter Layer

### JwtAuthenticationFilter (OncePerRequestFilter)
- `doFilterInternal(request, response, filterChain)`: JWT 토큰 추출 → 검증 → SecurityContext 설정 → 관리자 단일 세션 검증 → 토큰 자동 갱신

### StoreAccessFilter (OncePerRequestFilter)
- `doFilterInternal(request, response, filterChain)`: URL storeId vs 토큰 storeId 일치 검증

### SecurityConfig
- `filterChain(HttpSecurity) -> SecurityFilterChain`: 필터 체인 구성 (CSRF 비활성화, STATELESS, URL 패턴별 접근 제어)
- `passwordEncoder() -> PasswordEncoder`: BCryptPasswordEncoder

---

## Exception/Error Handling Layer

### AuthException (RuntimeException)
- `errorCode`: String (AUTHENTICATION_FAILED, TOKEN_MISSING, TOKEN_EXPIRED, TOKEN_INVALID)
- `message`: String

### GlobalExceptionHandler (@RestControllerAdvice)
- `handleAuthException(AuthException) -> ResponseEntity<ErrorResponse>`: 인증 예외 → 401
- `handleValidationException(MethodArgumentNotValidException) -> ResponseEntity<ErrorResponse>`: 검증 예외 → 400
- `handleAccessDeniedException(AccessDeniedException) -> ResponseEntity<ErrorResponse>`: 접근 거부 → 403
- `handleGenericException(Exception) -> ResponseEntity<ErrorResponse>`: 기타 → 500

### CustomAuthenticationEntryPoint
- `commence(request, response, authException)`: 401 에러 JSON 응답

### CustomAccessDeniedHandler
- `handle(request, response, accessDeniedException)`: 403 에러 JSON 응답

---

## DTO Layer

### Request DTOs
- `LoginRequest`: storeCode (String), username (String), password (String)
- `TableLoginRequest`: storeCode (String), tableNumber (Integer), password (String)
- `ChangePasswordRequest`: currentPassword (String), newPassword (String)

### Response DTOs
- `TokenResponse`: token (String), storeId (Long), role (String), userId (Long), tableId (Long), sessionId (Long)
- `ValidateResponse`: valid (boolean), role (String), storeId (Long), userId (Long), tableId (Long), sessionId (Long)
- `MessageResponse`: message (String)
- `ErrorResponse`: error (String), message (String), timestamp (String)

### Internal DTOs
- `AuthInfo`: userId (Long), storeId (Long), role (String), tableId (Long), sessionId (Long)
