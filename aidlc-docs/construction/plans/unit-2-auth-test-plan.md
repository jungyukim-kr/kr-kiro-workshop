# Test Plan for Unit 2: 인증 모듈

## Unit Overview
- **Unit**: unit-2-auth
- **Stories**: US-A01 (관리자 로그인), US-A03 (비밀번호 변경), US-C01 (테이블 로그인)
- **Approach**: TDD (RED-GREEN-REFACTOR)

---

## Business Logic Layer Tests

### JwtTokenProvider

- **TC-AUTH-001**: 관리자 AuthInfo로 JWT 토큰 생성 시 올바른 클레임 포함
  - Given: role=ADMIN, storeId=1, userId=1인 AuthInfo
  - When: generateToken() 호출
  - Then: 토큰에 sub="admin:1", storeId=1, role="ADMIN", userId=1 클레임 포함
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-002**: 테이블 AuthInfo로 JWT 토큰 생성 시 tableId, sessionId 포함
  - Given: role=TABLE, storeId=1, userId=5, tableId=5, sessionId=10인 AuthInfo
  - When: generateToken() 호출
  - Then: 토큰에 tableId=5, sessionId=10 클레임 포함
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-003**: 유효한 토큰 검증 시 AuthInfo 반환
  - Given: 정상 발급된 JWT 토큰
  - When: validateToken() 호출
  - Then: 원본 AuthInfo와 동일한 정보 반환
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-004**: 만료된 토큰 검증 시 ExpiredJwtException 발생
  - Given: 만료 시간이 과거인 JWT 토큰
  - When: validateToken() 호출
  - Then: ExpiredJwtException 발생
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-005**: 잘못된 서명의 토큰 검증 시 JwtException 발생
  - Given: 다른 secret으로 서명된 JWT 토큰
  - When: validateToken() 호출
  - Then: JwtException 발생
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-006**: 잔여시간 < refresh-threshold이면 shouldRefresh() true 반환
  - Given: 잔여시간이 refresh-threshold 미만인 토큰
  - When: shouldRefresh() 호출
  - Then: true 반환
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-007**: 잔여시간 >= refresh-threshold이면 shouldRefresh() false 반환
  - Given: 방금 발급된 토큰 (잔여시간 충분)
  - When: shouldRefresh() 호출
  - Then: false 반환
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-008**: refreshToken()으로 새 토큰 생성 시 동일 페이로드, 새 exp
  - Given: 유효한 JWT 토큰
  - When: refreshToken() 호출
  - Then: 동일 클레임, 새로운 exp를 가진 토큰 반환
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

### AuthService

- **TC-AUTH-009**: 관리자 로그인 성공 시 TokenResponse 반환
  - Given: 유효한 storeCode, username, password
  - When: adminLogin() 호출
  - Then: token, storeId, role="ADMIN", userId 포함된 TokenResponse 반환
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-010**: 관리자 로그인 시 존재하지 않는 매장 코드 → AuthException
  - Given: 존재하지 않는 storeCode
  - When: adminLogin() 호출
  - Then: AuthException (AUTHENTICATION_FAILED) 발생
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-011**: 관리자 로그인 시 존재하지 않는 username → AuthException
  - Given: 유효한 storeCode, 존재하지 않는 username
  - When: adminLogin() 호출
  - Then: AuthException (AUTHENTICATION_FAILED) 발생
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-012**: 관리자 로그인 시 비밀번호 불일치 → AuthException
  - Given: 유효한 storeCode, username, 잘못된 password
  - When: adminLogin() 호출
  - Then: AuthException (AUTHENTICATION_FAILED) 발생
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-013**: 관리자 로그인 성공 시 last_token_issued_at 갱신
  - Given: 유효한 로그인 정보
  - When: adminLogin() 호출
  - Then: Admin의 lastTokenIssuedAt이 현재 시각으로 갱신됨
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-014**: 테이블 로그인 성공 시 TokenResponse 반환 (활성 세션 있음)
  - Given: 유효한 storeCode, tableNumber, password + 활성 세션 존재
  - When: tableLogin() 호출
  - Then: token, storeId, role="TABLE", tableId, sessionId 포함된 TokenResponse 반환
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-015**: 테이블 로그인 성공 시 활성 세션 없으면 sessionId=null
  - Given: 유효한 로그인 정보 + 활성 세션 없음
  - When: tableLogin() 호출
  - Then: sessionId=null인 TokenResponse 반환
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-016**: 테이블 로그인 시 존재하지 않는 매장 → AuthException
  - Given: 존재하지 않는 storeCode
  - When: tableLogin() 호출
  - Then: AuthException (AUTHENTICATION_FAILED) 발생
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-017**: 테이블 로그인 시 PIN 불일치 → AuthException
  - Given: 유효한 storeCode, tableNumber, 잘못된 PIN
  - When: tableLogin() 호출
  - Then: AuthException (AUTHENTICATION_FAILED) 발생
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-018**: 비밀번호 변경 성공
  - Given: 유효한 adminId, 올바른 currentPassword, 새 newPassword
  - When: changePassword() 호출
  - Then: password_hash 갱신, lastTokenIssuedAt=null 설정
  - Story: US-A03
  - Status: ⬜ Not Started

- **TC-AUTH-019**: 비밀번호 변경 시 현재 비밀번호 불일치 → AuthException
  - Given: 유효한 adminId, 잘못된 currentPassword
  - When: changePassword() 호출
  - Then: AuthException 발생
  - Story: US-A03
  - Status: ⬜ Not Started

---

## API Layer Tests (MockMvc)

### AuthController

- **TC-AUTH-020**: POST /api/auth/admin/login 성공 → 200 + TokenResponse
  - Given: 유효한 LoginRequest JSON
  - When: POST /api/auth/admin/login
  - Then: 200 OK, TokenResponse body
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-021**: POST /api/auth/admin/login 인증 실패 → 401
  - Given: 잘못된 LoginRequest
  - When: POST /api/auth/admin/login
  - Then: 401, ErrorResponse body
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-022**: POST /api/auth/admin/login 입력 검증 실패 → 400
  - Given: storeCode 누락된 LoginRequest
  - When: POST /api/auth/admin/login
  - Then: 400, ErrorResponse body
  - Story: US-A01
  - Status: ⬜ Not Started

- **TC-AUTH-023**: POST /api/auth/table/login 성공 → 200 + TokenResponse
  - Given: 유효한 TableLoginRequest JSON
  - When: POST /api/auth/table/login
  - Then: 200 OK, TokenResponse body
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-024**: POST /api/auth/table/login PIN 형식 오류 → 400
  - Given: password="abc" (4자리 숫자 아님)
  - When: POST /api/auth/table/login
  - Then: 400, ErrorResponse body
  - Story: US-C01
  - Status: ⬜ Not Started

- **TC-AUTH-025**: PUT /api/auth/admin/password 성공 → 200
  - Given: 유효한 ADMIN JWT + ChangePasswordRequest
  - When: PUT /api/auth/admin/password
  - Then: 200, MessageResponse body
  - Story: US-A03
  - Status: ⬜ Not Started

- **TC-AUTH-026**: PUT /api/auth/admin/password 인증 없이 → 401
  - Given: Authorization 헤더 없음
  - When: PUT /api/auth/admin/password
  - Then: 401
  - Story: US-A03
  - Status: ⬜ Not Started

- **TC-AUTH-027**: GET /api/auth/validate 성공 → 200 + ValidateResponse
  - Given: 유효한 JWT
  - When: GET /api/auth/validate
  - Then: 200, ValidateResponse body
  - Story: US-A01, US-C01
  - Status: ⬜ Not Started

---

## Security Filter Tests

- **TC-AUTH-028**: JwtAuthenticationFilter - 유효한 토큰 시 SecurityContext 설정
  - Given: 유효한 Bearer 토큰이 포함된 요청
  - When: 필터 통과
  - Then: SecurityContext에 Authentication 설정됨
  - Status: ⬜ Not Started

- **TC-AUTH-029**: JwtAuthenticationFilter - 관리자 토큰의 단일 세션 검증 실패 시 401
  - Given: iat가 last_token_issued_at과 불일치하는 ADMIN 토큰
  - When: 필터 통과
  - Then: 401 TOKEN_INVALID 응답
  - Status: ⬜ Not Started

- **TC-AUTH-030**: JwtAuthenticationFilter - 토큰 갱신 필요 시 X-New-Token 헤더 추가
  - Given: 잔여시간 < 8시간인 토큰
  - When: 필터 통과
  - Then: 응답에 X-New-Token 헤더 포함
  - Status: ⬜ Not Started

- **TC-AUTH-031**: StoreAccessFilter - URL storeId와 토큰 storeId 불일치 시 403
  - Given: 토큰 storeId=1, URL /api/stores/2/admin/menus
  - When: 필터 통과
  - Then: 403 STORE_ACCESS_DENIED 응답
  - Status: ⬜ Not Started

- **TC-AUTH-032**: StoreAccessFilter - /api/auth/** 경로는 검증 생략
  - Given: /api/auth/validate 요청
  - When: 필터 통과
  - Then: storeId 검증 없이 통과
  - Status: ⬜ Not Started

---

## Requirements Coverage

| Requirement | Test Cases | Status |
|-------------|------------|--------|
| BR-AUTH-01.1 (관리자 인증 검증) | TC-AUTH-009~013 | ⬜ Pending |
| BR-AUTH-02.1 (테이블 인증 검증) | TC-AUTH-014~017 | ⬜ Pending |
| BR-AUTH-01.3 (단일 세션) | TC-AUTH-013, TC-AUTH-029 | ⬜ Pending |
| BR-AUTH-01.4 (비밀번호 변경) | TC-AUTH-018~019 | ⬜ Pending |
| BR-AUTH-03.1 (토큰 발급) | TC-AUTH-001~002 | ⬜ Pending |
| BR-AUTH-03.3 (토큰 검증) | TC-AUTH-003~005, TC-AUTH-028 | ⬜ Pending |
| BR-AUTH-03.4 (토큰 갱신) | TC-AUTH-006~008, TC-AUTH-030 | ⬜ Pending |
| BR-AUTH-03.5 (매장 격리) | TC-AUTH-031~032 | ⬜ Pending |
| BR-AUTH-04 (에러 처리) | TC-AUTH-021~022, TC-AUTH-024, TC-AUTH-026 | ⬜ Pending |
| BR-AUTH-05 (입력 검증) | TC-AUTH-022, TC-AUTH-024 | ⬜ Pending |
