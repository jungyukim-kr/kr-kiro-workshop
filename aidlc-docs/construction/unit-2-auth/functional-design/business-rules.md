# Unit 2: 인증 모듈 - Business Rules

## BR-AUTH-01: 관리자 로그인 규칙

### BR-AUTH-01.1: 인증 검증
- 매장 식별자(store_code)로 매장 존재 여부 확인
- (store_id, username) 조합으로 관리자 계정 조회
- bcrypt로 입력 비밀번호와 password_hash 비교
- 모든 검증 실패 시 동일한 일반 메시지 반환: "로그인 정보가 올바르지 않습니다"

### BR-AUTH-01.2: 로그인 시도 제한
- 로그인 시도 제한 없음 (계정 잠금 기능 미적용)
- 로그인 실패 시 일반 에러 메시지만 반환

### BR-AUTH-01.3: 관리자 동시 로그인 제한
- 관리자 계정은 단일 세션만 허용
- 새 로그인 시 이전 토큰 무효화 방식:
  - Admin 엔티티에 `last_token_issued_at` 필드 추가 (또는 별도 관리)
  - 토큰 검증 시 발급 시각이 `last_token_issued_at`과 일치하는지 확인
  - 불일치 시 토큰 무효 처리

### BR-AUTH-01.4: 관리자 비밀번호 변경
- 로그인된 관리자만 자신의 비밀번호 변경 가능
- 현재 비밀번호 확인 필수
- 새 비밀번호는 bcrypt로 해싱하여 저장
- 비밀번호 변경 후 기존 토큰 무효화 (재로그인 필요)

---

## BR-AUTH-02: 테이블 태블릿 인증 규칙

### BR-AUTH-02.1: 테이블 인증 검증
- 매장 식별자(store_code)로 매장 존재 여부 확인
- (store_id, table_number) 조합으로 테이블 조회
- bcrypt로 입력 PIN과 password_hash 비교
- 실패 시 일반 메시지: "로그인 정보가 올바르지 않습니다"

### BR-AUTH-02.2: 테이블 비밀번호 정책
- 4자리 숫자 PIN만 허용 (0000~9999)
- 정규식: `^\d{4}$`
- 로그인 시도 제한 없음 (물리적 접근 제어 전제)

### BR-AUTH-02.3: 테이블 동시 로그인
- 동일 테이블에 대한 동시 로그인 제한 없음
- 여러 기기에서 같은 테이블로 로그인 가능 (태블릿 교체 시나리오)

### BR-AUTH-02.4: 자동 로그인 (localStorage)
- 로그인 성공 시 클라이언트에서 localStorage에 저장:
  - `storeCode`: 매장 식별자
  - `tableNumber`: 테이블 번호
  - `password`: PIN (평문, 클라이언트 전용)
  - `token`: JWT 토큰
- 서비스 접속 시 localStorage 확인 → 저장된 정보로 자동 로그인 시도
- 자동 로그인 실패 시: 에러 메시지 표시 + 재시도 버튼 제공
- localStorage 정보는 삭제하지 않음 (재시도 가능하도록)

---

## BR-AUTH-03: JWT 토큰 규칙

### BR-AUTH-03.1: 토큰 발급
- 로그인 성공 시 JWT 토큰 발급
- 서명 알고리즘: HS256
- Secret Key: application.yml에서 환경변수로 관리
- 만료 시간: 16시간 (57,600초)

### BR-AUTH-03.2: 토큰 페이로드
- 관리자: `{sub, storeId, role:"ADMIN", userId, iat, exp}`
- 테이블: `{sub, storeId, role:"TABLE", userId, tableId, sessionId, iat, exp}`
- sessionId는 활성 세션이 없으면 null (첫 주문 전)

### BR-AUTH-03.3: 토큰 검증
- 모든 보호된 API 요청 시 Authorization 헤더에서 Bearer 토큰 추출
- 서명 검증 → 만료 검증 → 페이로드 추출
- 검증 실패 시 HTTP 401 Unauthorized 반환

### BR-AUTH-03.4: 토큰 갱신 (활동 감지 자동 연장)
- API 요청 시 토큰 잔여 시간 확인
- 잔여 시간이 전체 만료 시간의 50% 미만 (8시간 미만)이면 새 토큰 발급
- 새 토큰은 응답 헤더 `X-New-Token`에 포함
- 클라이언트는 `X-New-Token` 헤더가 있으면 localStorage의 토큰 갱신
- 토큰 만료 시 HTTP 401 반환 → 클라이언트에서 로그인 화면으로 이동

### BR-AUTH-03.5: 매장 격리
- JWT 페이로드의 storeId로 매장 격리
- 모든 API 요청 시 URL 경로의 storeId와 토큰의 storeId 일치 여부 검증
- 불일치 시 HTTP 403 Forbidden 반환

---

## BR-AUTH-04: 에러 처리 규칙

### BR-AUTH-04.1: 인증 에러 응답 형식
```json
{
  "error": "AUTHENTICATION_FAILED",
  "message": "로그인 정보가 올바르지 않습니다",
  "timestamp": "2026-03-06T13:00:00+09:00"
}
```

### BR-AUTH-04.2: 입력 검증 에러 응답
```json
{
  "error": "VALIDATION_FAILED",
  "message": "매장 식별자를 입력해주세요",
  "timestamp": "2026-03-06T13:00:00+09:00"
}
```

### BR-AUTH-04.3: 토큰 관련 에러
| 상황 | HTTP Status | Error Code |
|------|-------------|------------|
| 토큰 없음 | 401 | TOKEN_MISSING |
| 토큰 만료 | 401 | TOKEN_EXPIRED |
| 토큰 서명 불일치 | 401 | TOKEN_INVALID |
| 매장 권한 없음 | 403 | STORE_ACCESS_DENIED |
| 역할 권한 없음 | 403 | ROLE_ACCESS_DENIED |

---

## BR-AUTH-05: 입력 검증 규칙

| 필드 | 검증 규칙 | 에러 메시지 |
|------|-----------|------------|
| storeCode | 필수, 1~50자 | "매장 식별자를 입력해주세요" |
| username | 필수, 1~50자 | "사용자명을 입력해주세요" |
| password (관리자) | 필수, 1~255자 | "비밀번호를 입력해주세요" |
| password (테이블) | 필수, 정확히 4자리 숫자 | "4자리 숫자 PIN을 입력해주세요" |
| tableNumber | 필수, 양의 정수 | "테이블 번호를 입력해주세요" |
| newPassword (변경) | 필수, 8자리 이상 | "새 비밀번호는 8자리 이상이어야 합니다" |

---

## BR-AUTH-06: 보안 규칙

### BR-AUTH-06.1: 비밀번호 보안
- 모든 비밀번호는 bcrypt (cost factor 10)로 해싱
- 평문 비밀번호는 서버에 저장하지 않음
- 로그에 비밀번호 출력 금지

### BR-AUTH-06.2: JWT Secret 관리
- Secret Key는 환경변수 `JWT_SECRET`으로 관리
- 최소 256비트 (32바이트) 이상
- application.yml에 기본값 설정 (개발용), 운영 시 환경변수 오버라이드

### BR-AUTH-06.3: API 접근 제어
| API 경로 패턴 | 접근 권한 |
|---------------|-----------|
| POST /api/auth/admin/login | 인증 불필요 |
| POST /api/auth/table/login | 인증 불필요 |
| PUT /api/auth/admin/password | ADMIN 역할 |
| GET /api/auth/validate | 인증 필요 (모든 역할) |
| /api/stores/{storeId}/admin/** | ADMIN 역할 + storeId 일치 |
| /api/stores/{storeId}/customer/** | TABLE 역할 + storeId 일치 |
