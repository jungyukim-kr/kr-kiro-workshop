# Unit 2: 인증 모듈 - API 규격서

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080/api` |
| Content-Type | `application/json` |
| 인증 방식 | Bearer Token (JWT) |
| 문자 인코딩 | UTF-8 |

---

## 공통 헤더

### 요청 헤더

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Content-Type` | O | `application/json` |
| `Authorization` | 조건부 | `Bearer {JWT토큰}` (인증 필요 API) |

### 응답 헤더

| 헤더 | 조건 | 설명 |
|------|------|------|
| `X-New-Token` | 토큰 갱신 시 | 자동 갱신된 새 JWT 토큰. 클라이언트는 이 헤더가 있으면 localStorage의 토큰을 교체해야 함 |

---

## 공통 에러 응답 형식

```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 메시지",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

### 공통 에러 코드

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 400 | `VALIDATION_FAILED` | 입력 검증 실패 |
| 401 | `TOKEN_MISSING` | Authorization 헤더 없음 |
| 401 | `TOKEN_EXPIRED` | JWT 토큰 만료 |
| 401 | `TOKEN_INVALID` | JWT 서명 불일치 또는 단일 세션 위반 |
| 401 | `AUTHENTICATION_FAILED` | 로그인 인증 실패 |
| 403 | `STORE_ACCESS_DENIED` | URL의 storeId와 토큰의 storeId 불일치 |
| 403 | `ROLE_ACCESS_DENIED` | 역할 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

## API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | `/api/auth/admin/login` | 관리자 로그인 | 불필요 |
| 2 | POST | `/api/auth/table/login` | 테이블 태블릿 로그인 | 불필요 |
| 3 | PUT | `/api/auth/admin/password` | 관리자 비밀번호 변경 | ADMIN |
| 4 | GET | `/api/auth/validate` | 토큰 유효성 확인 | 모든 역할 |

---

## 1. 관리자 로그인

관리자가 매장 코드, 사용자명, 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.

### 요청

```
POST /api/auth/admin/login
Content-Type: application/json
```

#### Request Body

```json
{
  "storeCode": "STORE001",
  "username": "admin",
  "password": "password123"
}
```

#### 필드 상세

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| storeCode | String | O | 1~50자 | 매장 식별 코드 |
| username | String | O | 1~50자 | 관리자 사용자명 |
| password | String | O | 1~255자 | 관리자 비밀번호 |

### 응답

#### 성공 (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjoxIiwic3RvcmVJZCI6MSwicm9sZSI6IkFETUlOIiwidXNlcklkIjoxLCJpYXQiOjE3NDEyMzQ4MDAsImV4cCI6MTc0MTI5MjQwMH0.xxxxx",
  "storeId": 1,
  "role": "ADMIN",
  "userId": 1,
  "tableId": null,
  "sessionId": null
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| token | String | JWT 토큰 (만료: 16시간) |
| storeId | Long | 매장 ID |
| role | String | 역할 (`ADMIN`) |
| userId | Long | 관리자 ID |
| tableId | Long | 항상 `null` |
| sessionId | Long | 항상 `null` |

#### 실패 (401 Unauthorized)

```json
{
  "error": "AUTHENTICATION_FAILED",
  "message": "로그인 정보가 올바르지 않습니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

> 보안 정책: 매장 미존재, 계정 미존재, 비밀번호 불일치 모두 동일한 에러 메시지를 반환합니다.

#### 입력 검증 실패 (400 Bad Request)

```json
{
  "error": "VALIDATION_FAILED",
  "message": "매장 식별자를 입력해주세요",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

### 비즈니스 로직

1. `storeCode`로 매장(Store) 조회 → 없으면 인증 실패
2. `(store_id, username)`으로 관리자(Admin) 조회 → 없으면 인증 실패
3. bcrypt로 비밀번호 검증 → 불일치 시 인증 실패
4. 성공 시 `last_token_issued_at` 갱신 (단일 세션 관리)
5. JWT 토큰 생성 및 반환

### 참고사항

- 로그인 시도 제한 없음 (계정 잠금 기능 미적용)
- 새 로그인 시 이전 토큰은 자동 무효화됨 (단일 세션)

---

## 2. 테이블 태블릿 로그인

고객 태블릿에서 매장 코드, 테이블 번호, 4자리 PIN으로 로그인합니다.

### 요청

```
POST /api/auth/table/login
Content-Type: application/json
```

#### Request Body

```json
{
  "storeCode": "STORE001",
  "tableNumber": 5,
  "password": "1234"
}
```

#### 필드 상세

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| storeCode | String | O | 1~50자 | 매장 식별 코드 |
| tableNumber | Integer | O | 양의 정수 | 테이블 번호 |
| password | String | O | 정확히 4자리 숫자 (`^\d{4}$`) | 테이블 PIN |

### 응답

#### 성공 (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0YWJsZTo1Iiwic3RvcmVJZCI6MSwicm9sZSI6IlRBQkxFIiwidXNlcklkIjo1LCJ0YWJsZUlkIjo1LCJzZXNzaW9uSWQiOjEwLCJpYXQiOjE3NDEyMzQ4MDAsImV4cCI6MTc0MTI5MjQwMH0.xxxxx",
  "storeId": 1,
  "role": "TABLE",
  "userId": 5,
  "tableId": 5,
  "sessionId": 10
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| token | String | JWT 토큰 (만료: 16시간) |
| storeId | Long | 매장 ID |
| role | String | 역할 (`TABLE`) |
| userId | Long | 테이블 ID |
| tableId | Long | 테이블 ID |
| sessionId | Long \| null | 활성 세션 ID. 활성 세션이 없으면 `null` (첫 주문 시 세션 자동 생성) |

#### 실패 (401 Unauthorized)

```json
{
  "error": "AUTHENTICATION_FAILED",
  "message": "로그인 정보가 올바르지 않습니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

#### 입력 검증 실패 (400 Bad Request)

```json
{
  "error": "VALIDATION_FAILED",
  "message": "4자리 숫자 PIN을 입력해주세요",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

### 비즈니스 로직

1. `storeCode`로 매장(Store) 조회 → 없으면 인증 실패
2. `(store_id, table_number)`로 테이블(StoreTable) 조회 → 없으면 인증 실패
3. bcrypt로 PIN 검증 → 불일치 시 인증 실패
4. `(table_id, active=true)`로 활성 세션(TableSession) 조회
   - 있으면 → `sessionId` 포함
   - 없으면 → `sessionId = null`
5. JWT 토큰 생성 및 반환

### 참고사항

- 동일 테이블에 대한 동시 로그인 제한 없음
- 로그인 시도 제한 없음 (물리적 접근 제어 전제)
- 클라이언트는 로그인 성공 시 localStorage에 `storeCode`, `tableNumber`, `password`, `token` 저장 (자동 로그인용)

---

## 3. 관리자 비밀번호 변경

로그인된 관리자가 자신의 비밀번호를 변경합니다. 변경 후 기존 토큰이 무효화되므로 재로그인이 필요합니다.

### 요청

```
PUT /api/auth/admin/password
Content-Type: application/json
Authorization: Bearer {JWT토큰}
```

#### Request Body

```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

#### 필드 상세

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|-----------|------|
| currentPassword | String | O | 1~255자 | 현재 비밀번호 |
| newPassword | String | O | 8자 이상 | 새 비밀번호 |

### 응답

#### 성공 (200 OK)

```json
{
  "message": "비밀번호가 변경되었습니다. 다시 로그인해주세요."
}
```

#### 현재 비밀번호 불일치 (400 Bad Request)

```json
{
  "error": "VALIDATION_FAILED",
  "message": "현재 비밀번호가 올바르지 않습니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

#### 인증 실패 (401 Unauthorized)

```json
{
  "error": "TOKEN_MISSING",
  "message": "인증이 필요합니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

#### 권한 없음 (403 Forbidden)

```json
{
  "error": "ROLE_ACCESS_DENIED",
  "message": "접근 권한이 없습니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

### 비즈니스 로직

1. JWT 토큰에서 `role=ADMIN`, `userId` 확인
2. bcrypt로 현재 비밀번호 검증 → 불일치 시 400 반환
3. 새 비밀번호를 bcrypt로 해싱하여 `password_hash` 업데이트
4. `last_token_issued_at = null` 설정 (기존 토큰 무효화)
5. 클라이언트에서 localStorage 토큰 삭제 → 로그인 화면 이동

### 접근 제어

- 역할: `ADMIN`만 접근 가능

---

## 4. 토큰 유효성 확인

클라이언트 페이지 로드 시 저장된 토큰의 유효성을 확인합니다.

### 요청

```
GET /api/auth/validate
Authorization: Bearer {JWT토큰}
```

> Request Body 없음

### 응답

#### 성공 (200 OK) - 관리자 토큰

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

#### 성공 (200 OK) - 테이블 토큰

```json
{
  "valid": true,
  "role": "TABLE",
  "storeId": 1,
  "userId": 5,
  "tableId": 5,
  "sessionId": 10
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| valid | Boolean | 토큰 유효 여부 (항상 `true`, 무효 시 401 반환) |
| role | String | 역할 (`ADMIN` 또는 `TABLE`) |
| storeId | Long | 매장 ID |
| userId | Long | 사용자 ID (관리자 ID 또는 테이블 ID) |
| tableId | Long \| null | 테이블 ID (관리자는 `null`) |
| sessionId | Long \| null | 세션 ID (관리자는 `null`, 테이블은 활성 세션 없으면 `null`) |

#### 토큰 만료 (401 Unauthorized)

```json
{
  "error": "TOKEN_EXPIRED",
  "message": "토큰이 만료되었습니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

#### 토큰 무효 (401 Unauthorized)

```json
{
  "error": "TOKEN_INVALID",
  "message": "유효하지 않은 토큰입니다",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

### 접근 제어

- 역할: 모든 인증된 사용자 (`ADMIN`, `TABLE`)

---

## JWT 토큰 상세

### 토큰 구조

JWT 토큰은 Header, Payload, Signature 3개 파트로 구성됩니다.

#### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

#### Payload - 관리자 토큰

```json
{
  "sub": "admin:1",
  "storeId": 1,
  "role": "ADMIN",
  "userId": 1,
  "iat": 1741234800,
  "exp": 1741292400
}
```

#### Payload - 테이블 토큰

```json
{
  "sub": "table:5",
  "storeId": 1,
  "role": "TABLE",
  "userId": 5,
  "tableId": 5,
  "sessionId": 10,
  "iat": 1741234800,
  "exp": 1741292400
}
```

### 클레임 상세

| 클레임 | 타입 | 관리자 | 테이블 | 설명 |
|--------|------|--------|--------|------|
| sub | String | `admin:{adminId}` | `table:{tableId}` | 주체 식별자 |
| storeId | Long | O | O | 매장 ID |
| role | String | `ADMIN` | `TABLE` | 역할 |
| userId | Long | adminId | tableId | 사용자 ID |
| tableId | Long | - | O | 테이블 ID |
| sessionId | Long | - | O (nullable) | 활성 세션 ID |
| iat | Long | O | O | 발급 시각 (Unix timestamp) |
| exp | Long | O | O | 만료 시각 (Unix timestamp) |

### 토큰 설정값

| 항목 | 값 | 설명 |
|------|-----|------|
| 서명 알고리즘 | HS256 | HMAC-SHA256 |
| 만료 시간 | 57,600초 (16시간) | `app.jwt.expiration` |
| 갱신 임계값 | 28,800초 (8시간) | `app.jwt.refresh-threshold` |
| Secret Key | 환경변수 `JWT_SECRET` | 최소 256비트 (32바이트) |

### 토큰 자동 갱신 (Sliding Window)

API 요청 시 토큰 잔여 시간이 8시간 미만이면 새 토큰이 자동 발급됩니다.

- 새 토큰은 응답 헤더 `X-New-Token`에 포함
- 클라이언트는 모든 API 응답에서 `X-New-Token` 헤더를 확인하고, 있으면 localStorage의 토큰을 교체
- 관리자 토큰 갱신 시 DB의 `last_token_issued_at`도 함께 갱신

---

## Security Filter Chain

모든 보호된 API 요청은 다음 필터 체인을 순서대로 통과합니다.

```
HTTP Request
    │
    ▼
[1] CorsFilter ─── CORS 헤더 처리
    │
    ▼
[2] JwtAuthenticationFilter ─── 토큰 추출/검증/SecurityContext 설정/자동 갱신
    │
    ▼
[3] StoreAccessFilter ─── URL storeId vs 토큰 storeId 일치 검증
    │
    ▼
[4] AuthorizationFilter ─── 역할 기반 접근 제어 (ADMIN/TABLE)
    │
    ▼
[5] Controller
```

---

## URL 패턴별 접근 제어 매트릭스

| URL 패턴 | Method | 인증 | 역할 | 매장 격리 |
|----------|--------|------|------|-----------|
| `/api/auth/admin/login` | POST | 불필요 | - | - |
| `/api/auth/table/login` | POST | 불필요 | - | - |
| `/api/auth/admin/password` | PUT | 필요 | ADMIN | - |
| `/api/auth/validate` | GET | 필요 | 모든 역할 | - |
| `/api/stores/{storeId}/admin/**` | ALL | 필요 | ADMIN | O |
| `/api/stores/{storeId}/customer/**` | ALL | 필요 | TABLE | O |
| `/api/stores/{storeId}/events` | ALL | 필요 | ADMIN | O |

---

## 입력 검증 규칙 요약

| 필드 | 검증 규칙 | 에러 메시지 |
|------|-----------|------------|
| storeCode | 필수, 1~50자 | 매장 식별자를 입력해주세요 |
| username | 필수, 1~50자 | 사용자명을 입력해주세요 |
| password (관리자 로그인) | 필수, 1~255자 | 비밀번호를 입력해주세요 |
| password (테이블 로그인) | 필수, 정확히 4자리 숫자 | 4자리 숫자 PIN을 입력해주세요 |
| tableNumber | 필수, 양의 정수 | 테이블 번호를 입력해주세요 |
| currentPassword | 필수, 1~255자 | 현재 비밀번호를 입력해주세요 |
| newPassword | 필수, 8자 이상 | 새 비밀번호는 8자리 이상이어야 합니다 |
