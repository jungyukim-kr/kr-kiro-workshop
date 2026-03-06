# Unit 3: Customer Backend - NFR Requirements

## 성능 (Performance)

### API 응답 시간
- 주문 생성 API: < 1초
- 메뉴 조회 API: < 500ms
- 주문 내역 조회 API: < 500ms
- 맵기 옵션 조회 API: < 200ms

### 동시성 처리
- 주문 번호 채번: 낙관적 재시도 (Optimistic Retry)
  - order_number UNIQUE 제약조건 활용
  - 중복 발생 시 순번 +1로 재시도 (최대 3회)
  - DB Lock 사용하지 않음

## 보안 (Security)

### 인증/인가
- 모든 API는 JWT 인증 필수 (Unit 2 필터에서 처리)
- 역할: TABLE만 접근 가능
- 매장 격리: URL storeId와 JWT storeId 일치 검증 (Unit 2 StoreAccessFilter)

### 데이터 접근 범위
- 고객은 자신의 테이블/세션 데이터만 조회 가능
- 다른 테이블의 주문 조회 불가 (sessionId 기반 격리)

## 안정성 (Reliability)

### 트랜잭션 관리
- 세션 생성 + 주문 생성: 단일 트랜잭션 (전체 롤백)
- 부분 실패 시 세션도 주문도 생성되지 않음

### SSE 이벤트 발행
- 주문 생성과 SSE 발행은 독립적
- SSE 실패 시 주문은 성공 유지
- 관리자 화면에서 주기적 refresh로 누락 방지 (Unit 6 담당)

### 에러 처리
- Unit 2와 동일한 에러 응답 형식 사용
- 비즈니스 에러는 적절한 HTTP 상태 코드와 에러 코드 반환

## 유지보수성 (Maintainability)

### 패키지 구조
- `com.tableorder.customer.controller` — REST 컨트롤러
- `com.tableorder.customer.service` — 비즈니스 로직
- `com.tableorder.customer.dto` — 요청/응답 DTO
- `com.tableorder.customer.repository` — JPA Repository

### 계층 분리
- Controller: 요청 검증 + 응답 변환만
- Service: 비즈니스 로직 집중
- Repository: 데이터 접근만
