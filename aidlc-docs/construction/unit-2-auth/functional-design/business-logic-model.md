# Unit 2: 인증 모듈 - Business Logic Model

## 인증 플로우 개요

Unit 2는 두 가지 인증 경로를 제공합니다:
1. 관리자 로그인 (Admin Login)
2. 테이블 태블릿 로그인 (Table Login)

두 경로 모두 JWT 토큰을 발급하며, 이후 모든 API 요청은 토큰 기반으로 인증됩니다.

---

## 1. 관리자 로그인 플로우

### 플로우 다이어그램

```
[Admin UI]                    [AuthController]              [AuthService]              [DB]
    |                              |                            |                       |
    |-- POST /auth/admin/login --> |                            |                       |
    |   {storeCode, username,      |                            |                       |
    |    password}                 |-- adminLogin() ----------> |                       |
    |                              |                            |-- findStore() -------> |
    |                              |                            |<-- Store or null ----- |
    |                              |                            |                       |
    |                              |                            |   [Store 없음: 실패]   |
    |                              |                            |                       |
    |                              |                            |-- findAdmin() -------> |
    |                              |                            |<-- Admin or null ----- |
    |                              |                            |                       |
    |                              |                            |   [Admin 없음: 실패]   |
    |                              |                            |                       |
    |                              |                            |-- verifyPassword() --> |
    |                              |                            |   [불일치: 실패]        |
    |                              |                            |                       |
    |                              |                            |-- resetAttempts() ---> |
    |                              |                            |-- updateTokenTime() -> |
    |                              |                            |-- generateJWT() ----> |
    |                              |<-- JWT Token ------------- |                       |
    |<-- 200 {token, storeId} ---- |                            |                       |
```

### 상세 로직

**Step 1: 입력 검증**
- storeCode: 필수, 1~50자
- username: 필수, 1~50자
- password: 필수

**Step 2: 매장 확인**
- store_code로 Store 조회
- 없으면 → 일반 에러 메시지 반환 (매장 존재 여부 노출 방지)

**Step 3: 관리자 계정 조회**
- (store_id, username)으로 Admin 조회
- 없으면 → 일반 에러 메시지 반환

**Step 4: 비밀번호 검증**
- bcrypt.matches(password, password_hash)
- 불일치 시 → 일반 에러 메시지 반환

**Step 5: 로그인 성공 처리**
- last_token_issued_at = now (동시 로그인 제한용)
- JWT 토큰 생성 (role=ADMIN, storeId, userId)
- 응답: `{token, storeId, role}`

---

## 2. 테이블 태블릿 로그인 플로우

### 플로우 다이어그램

```
[Customer UI]                 [AuthController]              [AuthService]              [DB]
    |                              |                            |                       |
    |-- POST /auth/table/login --> |                            |                       |
    |   {storeCode, tableNumber,   |                            |                       |
    |    password}                 |-- tableLogin() ----------> |                       |
    |                              |                            |-- findStore() -------> |
    |                              |                            |<-- Store or null ----- |
    |                              |                            |                       |
    |                              |                            |-- findTable() -------> |
    |                              |                            |<-- Table or null ----- |
    |                              |                            |                       |
    |                              |                            |-- verifyPIN() -------> |
    |                              |                            |                       |
    |                              |                            |-- findActiveSession()> |
    |                              |                            |<-- Session or null --- |
    |                              |                            |                       |
    |                              |                            |-- generateJWT() ----> |
    |                              |<-- JWT Token ------------- |                       |
    |<-- 200 {token, storeId,      |                            |                       |
    |    tableId, sessionId} ----- |                            |                       |
```

### 상세 로직

**Step 1: 입력 검증**
- storeCode: 필수, 1~50자
- tableNumber: 필수, 양의 정수
- password: 필수, 정확히 4자리 숫자

**Step 2: 매장 확인**
- store_code로 Store 조회
- 없으면 → 일반 에러 메시지 반환

**Step 3: 테이블 조회**
- (store_id, table_number)로 StoreTable 조회
- 없으면 → 일반 에러 메시지 반환

**Step 4: PIN 검증**
- bcrypt.matches(password, password_hash)
- 불일치 시 → 일반 에러 메시지 반환
- 로그인 시도 제한 없음

**Step 5: 활성 세션 조회**
- (table_id, active=true)로 TableSession 조회
- 활성 세션이 있으면 → sessionId를 JWT에 포함
- 활성 세션이 없으면 → sessionId=null (첫 주문 시 세션 자동 생성)

**Step 6: 로그인 성공 처리**
- JWT 토큰 생성 (role=TABLE, storeId, userId=tableId, tableId, sessionId)
- 응답: `{token, storeId, tableId, sessionId}`

---

## 3. JWT 토큰 검증 플로우

### 플로우 다이어그램

```
[Client]                [SecurityFilter]           [JwtTokenProvider]
    |                        |                           |
    |-- API Request -------> |                           |
    |   Authorization:       |                           |
    |   Bearer {token}       |-- validateToken() ------> |
    |                        |                           |-- 서명 검증
    |                        |                           |-- 만료 검증
    |                        |                           |-- 페이로드 추출
    |                        |<-- AuthInfo ------------- |
    |                        |                           |
    |                        |-- [ADMIN] checkSingleSession()
    |                        |   iat == last_token_issued_at?
    |                        |                           |
    |                        |-- checkStoreAccess() ---> |
    |                        |   URL storeId == token storeId?
    |                        |                           |
    |                        |-- checkTokenRenewal() --> |
    |                        |   잔여시간 < 8시간?        |
    |                        |   → X-New-Token 헤더 추가  |
    |                        |                           |
    |<-- API Response ------- |                           |
    |   (+ X-New-Token)      |                           |
```

### 상세 로직

**Step 1: 토큰 추출**
- Authorization 헤더에서 "Bearer " 접두사 제거
- 토큰 없으면 → 401 TOKEN_MISSING

**Step 2: 서명 및 만료 검증**
- HS256 서명 검증 실패 → 401 TOKEN_INVALID
- 만료 시각 초과 → 401 TOKEN_EXPIRED

**Step 3: 관리자 단일 세션 검증 (role=ADMIN인 경우)**
- DB에서 Admin의 last_token_issued_at 조회
- 토큰의 iat와 비교
- 불일치 → 401 TOKEN_INVALID (다른 기기에서 로그인됨)

**Step 4: 매장 접근 검증**
- URL 경로의 storeId와 토큰의 storeId 비교
- 불일치 → 403 STORE_ACCESS_DENIED

**Step 5: 역할 기반 접근 제어**
- /admin/** 경로 → ADMIN 역할 필요
- /customer/** 경로 → TABLE 역할 필요
- 역할 불일치 → 403 ROLE_ACCESS_DENIED

**Step 6: 토큰 자동 갱신**
- 토큰 잔여 시간 계산: exp - now
- 잔여 시간 < 8시간 (전체의 50%) → 새 토큰 발급
- 새 토큰을 응답 헤더 `X-New-Token`에 추가
- 클라이언트는 이 헤더를 감지하여 localStorage 갱신

---

## 4. 관리자 비밀번호 변경 플로우

### 상세 로직

**Step 1: 인증 확인**
- JWT 토큰에서 role=ADMIN, userId 확인

**Step 2: 입력 검증**
- currentPassword: 필수
- newPassword: 필수, 8자리 이상

**Step 3: 현재 비밀번호 확인**
- bcrypt.matches(currentPassword, password_hash)
- 불일치 → 400 "현재 비밀번호가 올바르지 않습니다"

**Step 4: 비밀번호 업데이트**
- password_hash = bcrypt.encode(newPassword)
- last_token_issued_at = null (기존 토큰 무효화)

**Step 5: 응답**
- 200 "비밀번호가 변경되었습니다. 다시 로그인해주세요"
- 클라이언트에서 localStorage 토큰 삭제 → 로그인 화면 이동

---

## 5. 토큰 유효성 확인 API

### 용도
- 클라이언트 페이지 로드 시 저장된 토큰의 유효성 확인
- 자동 로그인 시도 전 토큰 상태 확인

### 상세 로직

**Endpoint**: GET /api/auth/validate

**Step 1: 토큰 검증** (SecurityFilter와 동일)

**Step 2: 응답**
```json
{
  "valid": true,
  "role": "ADMIN",
  "storeId": 1,
  "userId": 1,
  "tableId": null,
  "sessionId": null
}
```

---

## 6. AuthService 메서드 요약

| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| adminLogin(storeCode, username, password) | LoginRequest | TokenResponse | 관리자 로그인 |
| tableLogin(storeCode, tableNumber, password) | TableLoginRequest | TokenResponse | 테이블 로그인 |
| validateToken(token) | String | AuthInfo | 토큰 검증 |
| changePassword(adminId, currentPw, newPw) | ChangePasswordRequest | void | 비밀번호 변경 |

### TokenResponse 구조
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "storeId": 1,
  "role": "ADMIN",
  "userId": 1,
  "tableId": null,
  "sessionId": null
}
```

### AuthInfo 구조 (내부 객체)
```json
{
  "userId": 1,
  "storeId": 1,
  "role": "ADMIN",
  "tableId": null,
  "sessionId": null
}
```
