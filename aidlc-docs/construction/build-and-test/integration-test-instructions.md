# Integration Test Instructions

## 목적

Unit 1 (DB/Common)과 Unit 2 (인증 모듈) 간의 통합을 검증합니다.
실제 PostgreSQL DB와 Spring Boot 앱을 기동하여 전체 인증 흐름을 테스트합니다.

## 사전 조건

- Docker Desktop 실행 중
- PostgreSQL 컨테이너 기동 (`docker-compose up -d db`)
- 시드 데이터 로드 완료 (schema.sql + data.sql 자동 실행)

## 테스트 환경 구성

### 1. 서비스 기동

```bash
# DB 기동
docker-compose up -d db

# 앱 기동
$env:JAVA_HOME = "D:\DevLib\openjdk-17.0.0.1"
cd backend
.\gradlew.bat bootRun
```

앱이 `http://localhost:8080`에서 기동됩니다.

### 2. 시드 데이터 확인

| 데이터 | 값 |
|--------|-----|
| 매장 코드 | `STORE001` |
| 관리자 username | `admin` |
| 관리자 password | `admin123` |
| 테이블 1~5 PIN | `1234` |

---

## 통합 테스트 시나리오

### Scenario 1: 관리자 로그인 → 토큰 검증 → 비밀번호 변경

```bash
# Step 1: 관리자 로그인
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"admin123"}'

# 기대: 200 OK + TokenResponse (token, storeId=1, role=ADMIN)
# TOKEN 변수에 저장

# Step 2: 토큰 유효성 확인
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer {TOKEN}"

# 기대: 200 OK + ValidateResponse (valid=true, role=ADMIN)

# Step 3: 비밀번호 변경
curl -X PUT http://localhost:8080/api/auth/admin/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{"currentPassword":"admin123","newPassword":"newpass123"}'

# 기대: 200 OK + MessageResponse

# Step 4: 기존 토큰으로 재요청 (단일 세션 무효화 확인)
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer {TOKEN}"

# 기대: 401 Unauthorized (TOKEN_INVALID - last_token_issued_at=null)

# Step 5: 새 비밀번호로 재로그인
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"newpass123"}'

# 기대: 200 OK + 새 TokenResponse
```

### Scenario 2: 테이블 로그인 → 토큰 검증

```bash
# Step 1: 테이블 로그인
curl -X POST http://localhost:8080/api/auth/table/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","tableNumber":1,"password":"1234"}'

# 기대: 200 OK + TokenResponse (role=TABLE, tableId, sessionId)

# Step 2: 토큰 유효성 확인
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer {TOKEN}"

# 기대: 200 OK + ValidateResponse (valid=true, role=TABLE)
```

### Scenario 3: 매장 격리 검증

```bash
# Step 1: 매장 1 관리자로 로그인
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"admin123"}'

# Step 2: 다른 매장 리소스 접근 시도
curl -X GET http://localhost:8080/api/stores/999/admin/menus \
  -H "Authorization: Bearer {TOKEN}"

# 기대: 403 Forbidden (STORE_ACCESS_DENIED)
```

### Scenario 4: 인증 실패 케이스

```bash
# 잘못된 비밀번호
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"wrong"}'

# 기대: 401 (AUTHENTICATION_FAILED)

# 존재하지 않는 매장
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"INVALID","username":"admin","password":"admin123"}'

# 기대: 401 (AUTHENTICATION_FAILED)

# 토큰 없이 보호된 API 접근
curl -X GET http://localhost:8080/api/auth/validate

# 기대: 401 (TOKEN_MISSING)

# 잘못된 PIN 형식
curl -X POST http://localhost:8080/api/auth/table/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","tableNumber":1,"password":"abc"}'

# 기대: 400 (VALIDATION_FAILED)
```

### Scenario 5: 관리자 단일 세션 검증

```bash
# Step 1: 첫 번째 로그인
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"admin123"}'
# TOKEN_1 저장

# Step 2: 두 번째 로그인 (다른 기기 시뮬레이션)
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"admin123"}'
# TOKEN_2 저장

# Step 3: 첫 번째 토큰으로 요청
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer {TOKEN_1}"

# 기대: 401 (TOKEN_INVALID - iat가 last_token_issued_at과 불일치)

# Step 4: 두 번째 토큰으로 요청
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer {TOKEN_2}"

# 기대: 200 OK (최신 토큰만 유효)
```

## 정리

```bash
# 앱 종료 (Ctrl+C 또는)
docker-compose down
```
