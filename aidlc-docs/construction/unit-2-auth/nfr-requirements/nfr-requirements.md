# Unit 2: 인증 모듈 - NFR Requirements

## NFR-AUTH-01: 성능 요구사항

### NFR-AUTH-01.1: 인증 API 응답 시간
- 로그인 API (관리자/테이블): 1초 이내
- JWT 토큰 검증: 10ms 이내 (서명 검증만, DB 조회 제외)
- 관리자 단일 세션 검증 (DB 조회 포함): 100ms 이내
- 비밀번호 변경 API: 1초 이내

### NFR-AUTH-01.2: bcrypt 해싱 성능
- Cost Factor: 10 (기본값)
- 예상 해싱 시간: ~100ms
- 로그인 응답 시간의 주요 병목 요소
- 전체 로그인 응답 = bcrypt (~100ms) + DB 조회 (~10ms) + JWT 생성 (~5ms) ≈ 115ms

### NFR-AUTH-01.3: 동시 접속
- 매장당 테이블 수 기준 동시 인증 요청 처리
- 예상 피크: 매장당 최대 50개 테이블 동시 로그인
- Spring Boot 기본 스레드 풀 (200)로 충분

---

## NFR-AUTH-02: 보안 요구사항

### NFR-AUTH-02.1: 비밀번호 보안
- 해싱 알고리즘: bcrypt (cost factor 10)
- 평문 비밀번호 서버 저장 금지
- 로그 출력 시 비밀번호 마스킹
- 관리자 비밀번호 변경 시 현재 비밀번호 확인 필수

### NFR-AUTH-02.2: JWT 토큰 보안
- 서명 알고리즘: HS256
- Secret Key: 환경변수 `JWT_SECRET`으로 관리
- Secret Key 최소 길이: 256비트 (32바이트)
- 토큰 만료: 16시간
- 토큰 저장: localStorage (MVP 단순화)
- 토큰 전송: Authorization Bearer 헤더

### NFR-AUTH-02.3: CORS 정책
- 개발 환경: 모든 origin 허용 (`*`)
- 운영 환경: 특정 도메인만 허용 (application.yml에서 설정)
- 허용 메서드: GET, POST, PUT, DELETE, OPTIONS
- 허용 헤더: Authorization, Content-Type, X-New-Token
- 노출 헤더: X-New-Token (토큰 갱신용)

### NFR-AUTH-02.4: API 접근 제어
- 공개 API: `/api/auth/**` (로그인 엔드포인트)
- 보호 API: 그 외 모든 `/api/**` 경로
- 역할 기반 접근 제어: ADMIN, TABLE
- 매장 격리: JWT storeId와 URL storeId 일치 검증

---

## NFR-AUTH-03: 가용성 요구사항

### NFR-AUTH-03.1: 서비스 가용성
- 단일 인스턴스 운영 (MVP)
- Spring Boot 내장 서버 기반
- 서버 재시작 시 기존 JWT 토큰 유효 (stateless 특성)

### NFR-AUTH-03.2: 토큰 갱신 실패 복구
- 토큰 만료 시: 클라이언트에서 로그인 화면으로 리다이렉트
- 테이블: localStorage에 저장된 정보로 자동 재로그인 시도
- 관리자: 수동 재로그인 필요
- X-New-Token 갱신 실패 시: 기존 토큰 계속 사용 (만료까지)

---

## NFR-AUTH-04: 로깅 요구사항

### NFR-AUTH-04.1: 표준 로깅 수준
- 로그인 성공: INFO 레벨 (사용자, 매장, 역할, IP)
- 로그인 실패: WARN 레벨 (매장, 사용자명, 실패 사유)
- 토큰 발급: INFO 레벨 (사용자, 만료 시각)
- 토큰 검증 실패: WARN 레벨 (실패 사유, 요청 경로)
- 토큰 갱신: DEBUG 레벨 (사용자, 잔여 시간)
- 비밀번호 변경: INFO 레벨 (사용자, 매장)

### NFR-AUTH-04.2: 로깅 형식
- SLF4J + Logback (Spring Boot 기본)
- 로그 패턴: `[timestamp] [level] [class] - message`
- 비밀번호, 토큰 값은 로그에 절대 출력하지 않음

---

## NFR-AUTH-05: 에러 응답 요구사항

### NFR-AUTH-05.1: 표준 에러 응답 형식
```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 메시지",
  "timestamp": "2026-03-06T14:00:00+09:00"
}
```

### NFR-AUTH-05.2: HTTP 상태 코드 매핑
| 상황 | HTTP Status | Error Code |
|------|-------------|------------|
| 인증 실패 | 401 | AUTHENTICATION_FAILED |
| 토큰 없음 | 401 | TOKEN_MISSING |
| 토큰 만료 | 401 | TOKEN_EXPIRED |
| 토큰 무효 | 401 | TOKEN_INVALID |
| 매장 접근 거부 | 403 | STORE_ACCESS_DENIED |
| 역할 접근 거부 | 403 | ROLE_ACCESS_DENIED |
| 입력 검증 실패 | 400 | VALIDATION_FAILED |
| 서버 오류 | 500 | INTERNAL_ERROR |
