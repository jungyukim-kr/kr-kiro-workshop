# TDD Code Generation Plan for Unit 2: 인증 모듈

## Unit Context
- **Workspace Root**: D:\workspace\kr-kiro-workshop
- **Project Type**: Greenfield (모노레포, 모놀리식 Spring Boot)
- **Stories**: US-A01 (관리자 로그인), US-A03 (비밀번호 변경), US-C01 (테이블 로그인)
- **Approach**: TDD (RED-GREEN-REFACTOR)
- **Code Location**: `backend/src/main/java/com/tableorder/auth/`
- **Test Location**: `backend/src/test/java/com/tableorder/auth/`

## Unit 1 기존 코드 수정 사항 (중복 제외)
- schema.sql: admin 테이블에 `last_token_issued_at` 컬럼 추가만
- application.yml: `refresh-threshold` 설정 추가만
- Admin.java: `lastTokenIssuedAt` 필드 추가만
- SecurityConfig.java: auth 패키지로 이동하여 교체
- build.gradle: test 의존성 추가만 (spring-boot-starter-test, spring-security-test)

---

### Plan Step 0: Unit 1 기존 코드 수정 + 프로젝트 구조 설정
- [x] 0.1: schema.sql에 `last_token_issued_at TIMESTAMP` 컬럼 추가
- [x] 0.2: application.yml에 `app.jwt.refresh-threshold-ms: 28800000` 추가, CORS 설정 추가
- [x] 0.3: Admin.java에 `lastTokenIssuedAt` 필드 + getter/setter 추가
- [x] 0.4: build.gradle에 test/validation 의존성 추가
- [x] 0.5: auth 패키지 디렉토리 구조 생성 (controller, service, security, dto, exception)
- [x] 0.6: DTO 클래스 생성 (LoginRequest, TableLoginRequest, ChangePasswordRequest, TokenResponse, ValidateResponse, MessageResponse, ErrorResponse, AuthInfo)
- [x] 0.7: AuthException 클래스 생성
- [x] 0.8: Repository 인터페이스 생성 (AdminRepository, StoreRepository, StoreTableRepository, TableSessionRepository)
- [x] 0.9: 컴파일 확인 ✅

### Plan Step 1: Business Logic Layer - JwtTokenProvider (TDD)
- [x] 1.1: JwtTokenProvider skeleton 생성
- [x] 1.2: generateToken() + validateToken() - RED-GREEN-REFACTOR
- [x] 1.3: validateToken() 실패 케이스 - RED-GREEN-REFACTOR
- [x] 1.4: shouldRefresh() + refreshToken() - RED-GREEN-REFACTOR

### Plan Step 2: Business Logic Layer - AuthService (TDD)
- [x] 2.1: AuthService skeleton 생성
- [x] 2.2: adminLogin() - RED-GREEN-REFACTOR (TC-AUTH-009~013)
- [x] 2.3: tableLogin() - RED-GREEN-REFACTOR (TC-AUTH-014~017)
- [x] 2.4: changePassword() - RED-GREEN-REFACTOR (TC-AUTH-018~019)

### Plan Step 3: Security Filter Layer (TDD)
- [x] 3.1: JwtAuthenticationFilter skeleton 생성
- [x] 3.2: JwtAuthenticationFilter - RED-GREEN-REFACTOR (TC-AUTH-028~030)
- [x] 3.3: StoreAccessFilter skeleton 생성
- [x] 3.4: StoreAccessFilter - RED-GREEN-REFACTOR (TC-AUTH-031~032)
- [x] 3.5: CustomAuthenticationEntryPoint + CustomAccessDeniedHandler 생성
- [x] 3.6: SecurityConfig 생성 (필터 체인 조립, URL 패턴별 접근 제어)

### Plan Step 4: API Layer - AuthController (TDD)
- [x] 4.1: AuthController skeleton 생성
- [x] 4.2: POST /api/auth/admin/login - RED-GREEN-REFACTOR
  - [x] RED: TC-AUTH-020 (성공 → 200)
  - [x] GREEN: adminLogin 엔드포인트 구현
  - [x] RED: TC-AUTH-021 (인증 실패 → 401)
  - [x] GREEN: 에러 처리 확인
  - [x] RED: TC-AUTH-022 (입력 검증 실패 → 400)
  - [x] GREEN: @Valid 검증 추가
  - [x] REFACTOR
  - [x] VERIFY: TC-AUTH-020~022 모두 통과
- [x] 4.3: POST /api/auth/table/login - RED-GREEN-REFACTOR
  - [x] RED: TC-AUTH-023 (성공 → 200)
  - [x] GREEN: tableLogin 엔드포인트 구현
  - [x] RED: TC-AUTH-024 (PIN 형식 오류 → 400)
  - [x] GREEN: PIN 검증 추가
  - [x] REFACTOR
  - [x] VERIFY: TC-AUTH-023~024 모두 통과
- [x] 4.4: PUT /api/auth/admin/password - RED-GREEN-REFACTOR
  - [x] RED: TC-AUTH-025 (성공 → 200)
  - [x] GREEN: changePassword 엔드포인트 구현
  - [x] RED: TC-AUTH-026 (인증 없이 → 401)
  - [x] GREEN: 인증 확인
  - [x] REFACTOR
  - [x] VERIFY: TC-AUTH-025~026 모두 통과
- [x] 4.5: GET /api/auth/validate - RED-GREEN-REFACTOR
  - [x] RED: TC-AUTH-027 (성공 → 200)
  - [x] GREEN: validate 엔드포인트 구현
  - [x] REFACTOR
  - [x] VERIFY: TC-AUTH-027 통과
- [x] 4.6: GlobalExceptionHandler 생성

### Plan Step 5: Documentation + Final Verification
- [x] 5.1: 전체 테스트 실행 (32개 TC 모두 통과 확인)
- [x] 5.2: Gradle 빌드 확인
- [x] 5.3: code-summary.md 생성 (aidlc-docs/construction/unit-2-auth/code/)
- [x] 5.4: aidlc-state.md 업데이트
