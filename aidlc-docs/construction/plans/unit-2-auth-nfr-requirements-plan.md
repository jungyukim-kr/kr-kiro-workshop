# Unit 2: 인증 모듈 - NFR Requirements Plan

## Unit 정보
- **Unit 이름**: Unit 2 - 인증 모듈 (Authentication Module)
- **팀**: 공통 (양 팀 협업)
- **범위**: 관리자 로그인, 테이블 태블릿 인증, JWT 발급/검증, bcrypt, 비밀번호 변경

## NFR Requirements 체크리스트

### 1. 성능 요구사항
- [x] 인증 API 응답 시간 기준 정의
- [x] JWT 토큰 검증 성능 기준 정의
- [x] bcrypt 해싱 성능 영향 분석

### 2. 보안 요구사항
- [x] JWT Secret 관리 방식 정의
- [x] bcrypt cost factor 결정
- [x] CORS 정책 정의
- [x] API 접근 제어 정책 정의

### 3. 가용성 요구사항
- [x] 인증 서비스 가용성 기준 정의
- [x] 토큰 갱신 실패 시 복구 전략

### 4. 기술 스택 결정
- [x] JWT 라이브러리 선정
- [x] Spring Security 설정 방식 결정
- [x] 비밀번호 해싱 라이브러리 확인

---

## 질문 섹션

### Q1: 인증 API 응답 시간 목표
인증 API의 응답 시간 목표는 어느 정도인가요?

A) 100ms 이내 (고성능)
B) 500ms 이내 (일반적)
C) 1초 이내 (여유)
D) 특별한 기준 없음 (합리적 수준이면 OK)
E) Other (설명해주세요)

[Answer]:C

### Q2: bcrypt Cost Factor
bcrypt 해싱의 cost factor (라운드 수)는 어떻게 설정할까요?
높을수록 보안은 강하지만 로그인 시 해싱 시간이 길어집니다.

A) 10 (기본값, ~100ms, 일반적 웹 서비스)
B) 12 (~300ms, 보안 강화)
C) 14 (~1초, 높은 보안)
D) 8 (~25ms, 빠른 응답 우선)
E) Other (설명해주세요)

[Answer]:A

### Q3: JWT 라이브러리 선택
Spring Boot에서 사용할 JWT 라이브러리는?

A) jjwt (io.jsonwebtoken) - 가장 널리 사용, 안정적
B) Spring Security OAuth2 Resource Server - Spring 공식 지원
C) Nimbus JOSE+JWT - 표준 준수, 유연
D) Auth0 java-jwt - 간결한 API
E) Other (설명해주세요)

[Answer]:A

### Q4: Spring Security 필터 체인 구성
Spring Security 설정 방식은?

A) SecurityFilterChain Bean 방식 (Spring Security 6.x 권장)
B) WebSecurityConfigurerAdapter 상속 (레거시, deprecated)
C) 커스텀 필터만 사용 (Spring Security 최소 활용)
D) Spring Security 사용하지 않음 (직접 필터 구현)
E) Other (설명해주세요)

[Answer]:A

### Q5: CORS 정책
CORS 설정은 어떻게 할까요?

A) 개발 환경: 모든 origin 허용 / 운영: 특정 도메인만
B) 모든 환경에서 모든 origin 허용 (MVP 단순화)
C) 프론트엔드와 백엔드가 같은 origin (CORS 불필요)
D) 프록시 서버 사용 (nginx 등)
E) Other (설명해주세요)

[Answer]:A

### Q6: 로깅 수준
인증 관련 로깅은 어느 수준으로 할까요?

A) 최소 로깅 (로그인 성공/실패만)
B) 표준 로깅 (성공/실패 + 토큰 발급/검증)
C) 상세 로깅 (모든 인증 이벤트 + 요청 정보)
D) 감사 로깅 (상세 + 별도 감사 로그 파일)
E) Other (설명해주세요)

[Answer]:B

### Q7: 에러 응답 형식
API 에러 응답의 표준 형식은?

A) 단순 형식 `{error, message, timestamp}`
B) RFC 7807 Problem Details `{type, title, status, detail, instance}`
C) Spring Boot 기본 에러 형식
D) 커스텀 형식 (설명해주세요)
E) Other (설명해주세요)

[Answer]:A

---

## 다음 단계
모든 질문에 답변하신 후, AI가 답변을 분석하고 NFR Requirements 산출물을 생성합니다.
