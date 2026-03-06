# Unit 2: 인증 모듈 - NFR Design Patterns

## DP-AUTH-01: JWT Stateless 인증 패턴

### 패턴 설명
서버에 세션 상태를 저장하지 않고, JWT 토큰 자체에 인증 정보를 포함하여 매 요청마다 독립적으로 검증하는 패턴.

### 적용 방식
- 로그인 성공 시 JWT 토큰 발급 (서버 세션 없음)
- 클라이언트가 토큰을 localStorage에 저장
- 매 API 요청 시 Authorization Bearer 헤더로 전송
- 서버는 토큰 서명과 만료만 검증 (DB 조회 최소화)

### 장점
- 서버 재시작 시에도 기존 토큰 유효
- 수평 확장 용이 (향후)
- 서버 메모리 부담 없음

### 예외: 관리자 단일 세션
- 관리자 토큰은 DB의 last_token_issued_at과 비교 필요 (DP-AUTH-04 참조)
- 테이블 토큰은 순수 stateless

---

## DP-AUTH-02: Security Filter Chain 패턴

### 패턴 설명
Spring Security의 필터 체인을 활용하여 인증/인가를 계층적으로 처리하는 패턴.

### 필터 체인 순서

```
HTTP Request
    |
    v
[1] CorsFilter (Spring 내장)
    - CORS 헤더 처리
    - Preflight OPTIONS 요청 처리
    |
    v
[2] JwtAuthenticationFilter (커스텀, OncePerRequestFilter)
    - Authorization 헤더에서 토큰 추출
    - 토큰 서명/만료 검증
    - SecurityContext에 Authentication 설정
    - 토큰 자동 갱신 (X-New-Token)
    |
    v
[3] StoreAccessFilter (커스텀, OncePerRequestFilter)
    - URL의 storeId와 토큰의 storeId 비교
    - 불일치 시 403 반환
    |
    v
[4] AuthorizationFilter (Spring Security 내장)
    - 역할 기반 접근 제어 (ADMIN/TABLE)
    - URL 패턴별 역할 매핑
    |
    v
[5] Controller
```

### 공개 경로 (필터 우회)
- `POST /api/auth/admin/login`
- `POST /api/auth/table/login`

---

## DP-AUTH-03: Role-Based Access Control (RBAC) 패턴

### 패턴 설명
JWT 토큰의 role 클레임을 기반으로 API 접근을 제어하는 패턴.

### 역할 정의

| 역할 | 설명 | 접근 가능 경로 |
|------|------|---------------|
| ADMIN | 관리자 | `/api/auth/admin/**`, `/api/stores/{storeId}/admin/**` |
| TABLE | 테이블 태블릿 | `/api/stores/{storeId}/customer/**` |

### 매장 격리 (Store Isolation)
- 모든 보호된 API는 URL에 `{storeId}` 포함
- StoreAccessFilter에서 토큰의 storeId와 URL의 storeId 비교
- 다른 매장 데이터 접근 원천 차단

### SecurityFilterChain 설정 예시
```
/api/auth/**                          → permitAll
/api/auth/admin/password              → hasRole(ADMIN)
/api/stores/{storeId}/admin/**        → hasRole(ADMIN)
/api/stores/{storeId}/customer/**     → hasRole(TABLE)
/api/stores/{storeId}/events          → hasRole(ADMIN)
```

---

## DP-AUTH-04: 관리자 단일 세션 패턴

### 패턴 설명
관리자 계정의 동시 로그인을 제한하여 단일 세션만 허용하는 패턴.

### 구현 방식: DB last_token_issued_at

**로그인 시**:
1. JWT 토큰 생성 (iat = 현재 시각)
2. Admin 테이블의 `last_token_issued_at` = 현재 시각 저장
3. 토큰 반환

**요청 검증 시** (JwtAuthenticationFilter):
1. 토큰에서 role 확인
2. role=ADMIN인 경우:
   - DB에서 Admin의 last_token_issued_at 조회
   - 토큰의 iat와 비교
   - 불일치 → 401 TOKEN_INVALID (다른 기기에서 로그인됨)
3. role=TABLE인 경우: 검증 생략 (동시 로그인 허용)

### Admin 엔티티 추가 필드
```
last_token_issued_at TIMESTAMP NULLABLE  -- 마지막 토큰 발급 시각
```

### 성능 영향
- 관리자 요청에만 DB 조회 1회 추가
- 관리자 요청 빈도가 낮아 영향 미미
- 테이블 요청은 영향 없음 (순수 stateless)

---

## DP-AUTH-05: 토큰 자동 갱신 패턴 (Sliding Window)

### 패턴 설명
사용자 활동이 감지되면 토큰 만료를 자동으로 연장하는 패턴.

### 구현 방식

**JwtAuthenticationFilter에서 처리**:
1. 토큰 검증 성공 후 잔여 시간 계산
2. 잔여 시간 < 8시간 (전체 16시간의 50%):
   - 새 토큰 생성 (동일 페이로드, 새 exp)
   - 관리자: last_token_issued_at도 갱신
   - 응답 헤더에 `X-New-Token: {새토큰}` 추가
3. 잔여 시간 >= 8시간: 갱신 안 함

**클라이언트 처리**:
- 모든 API 응답에서 `X-New-Token` 헤더 확인
- 헤더가 있으면 localStorage의 토큰 교체

### Secret Key 관리
- 환경변수 `JWT_SECRET`에서 로드
- 서버 재시작 시 새 키 적용 가능 (기존 토큰 무효화)
- MVP에서는 고정 키 사용

---

## DP-AUTH-06: 통합 에러 처리 패턴

### 패턴 설명
인증/인가 관련 에러를 일관된 형식으로 처리하는 패턴.

### 구현 방식

**AuthenticationEntryPoint** (401 처리):
- 토큰 없음, 만료, 무효 시 호출
- 표준 에러 응답 형식으로 반환

**AccessDeniedHandler** (403 처리):
- 역할 불일치, 매장 접근 거부 시 호출
- 표준 에러 응답 형식으로 반환

**GlobalExceptionHandler** (@RestControllerAdvice):
- 비즈니스 예외 (인증 실패, 검증 실패) 처리
- 표준 에러 응답 형식으로 반환

### 에러 응답 형식
```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 메시지",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```
