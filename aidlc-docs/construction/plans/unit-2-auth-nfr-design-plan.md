# Unit 2: 인증 모듈 - NFR Design Plan

## Unit 정보
- **Unit 이름**: Unit 2 - 인증 모듈 (Authentication Module)
- **팀**: 공통 (양 팀 협업)

## NFR Design 체크리스트

### 1. 보안 패턴 설계
- [x] JWT 인증 필터 체인 설계
- [x] 역할 기반 접근 제어 (RBAC) 패턴 설계
- [x] 매장 격리 패턴 설계

### 2. 성능 패턴 설계
- [x] 토큰 자동 갱신 패턴 설계
- [x] Stateless 인증 패턴 설계

### 3. 논리적 컴포넌트 설계
- [x] Spring Security 필터 체인 컴포넌트 구조
- [x] JWT 관련 컴포넌트 구조
- [x] 에러 처리 컴포넌트 구조

---

## 질문 섹션

### Q1: JWT Secret Key 로테이션
JWT Secret Key 변경이 필요한 경우 어떻게 처리할까요?

A) 서버 재시작 시 새 키 적용 (기존 토큰 무효화, 재로그인 필요)
B) 이전 키와 새 키 동시 지원 (Grace Period)
C) 키 로테이션 불필요 (MVP에서는 고정 키 사용)
E) Other (설명해주세요)

[Answer]:A

### Q2: 인증 실패 시 응답 지연 (Timing Attack 방어)
존재하지 않는 계정으로 로그인 시도 시, bcrypt 비교를 건너뛰면 응답 시간 차이로 계정 존재 여부를 유추할 수 있습니다.

A) Dummy bcrypt 비교 수행 (일정한 응답 시간 유지, 보안 강화)
B) 응답 지연 불필요 (MVP에서는 단순 처리)
E) Other (설명해주세요)

[Answer]:B

### Q3: 관리자 단일 세션 무효화 방식
관리자가 다른 기기에서 로그인하면 이전 세션을 무효화해야 합니다.

A) DB에 last_token_issued_at 저장, 매 요청 시 DB 조회로 검증
B) 인메모리 캐시 (HashMap)에 저장, 서버 재시작 시 초기화
C) DB 저장 + 인메모리 캐시 조합 (캐시 미스 시 DB 조회)
E) Other (설명해주세요)

[Answer]:추천해줘.

---

## 다음 단계
모든 질문에 답변하신 후, NFR Design 산출물을 생성합니다.
