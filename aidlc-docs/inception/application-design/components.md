# Application Design - Components

## 시스템 개요

```
+------------------+       +------------------+       +------------+
|  Customer UI     |------>|  Spring Boot     |------>| PostgreSQL |
|  (Vanilla JS)    |<------|  REST API + SSE  |<------| Database   |
+------------------+       +------------------+       +------------+
        |                         ^
        |                         |
+------------------+              |
|  Admin UI        |--------------+
|  (Vanilla JS)    |
+------------------+
```

## 컴포넌트 목록

### 1. AuthComponent
- **목적**: 인증/인가 처리
- **책임**:
  - 관리자 로그인 (매장ID + 사용자명 + 비밀번호)
  - JWT 토큰 발급/검증 (16시간 만료)
  - 테이블 태블릿 인증 (매장ID + 테이블번호 + 비밀번호)
  - 로그인 시도 제한
- **인터페이스**: REST API (`/api/auth/*`)

### 2. StoreComponent
- **목적**: 매장 정보 관리
- **책임**:
  - 매장 기본 정보 조회
  - 다중 매장 데이터 격리
- **인터페이스**: REST API (`/api/stores/*`)

### 3. MenuComponent
- **목적**: 메뉴 관리
- **책임**:
  - 메뉴 CRUD (메뉴명, 가격, 설명, 카테고리, 이미지URL)
  - 맵기 수준/옵션 관리
  - 카테고리별 조회
  - 노출 순서 관리
  - 필수 필드/가격 범위 검증
- **인터페이스**: REST API (`/api/stores/{storeId}/menus/*`)

### 4. TableComponent
- **목적**: 테이블 및 세션 관리
- **책임**:
  - 테이블 초기 설정 (번호, 비밀번호)
  - 테이블 세션 라이프사이클 (시작/종료)
  - 세션 시작: 첫 주문 시 자동 생성
  - 세션 종료: 이용 완료 처리 → OrderHistory 이동
- **인터페이스**: REST API (`/api/stores/{storeId}/tables/*`)

### 5. OrderComponent
- **목적**: 주문 처리
- **책임**:
  - 주문 생성 (메뉴 목록, 수량, 요청사항, 맵기 옵션)
  - 주문 상태 변경 (대기중→준비중→완료)
  - 주문 삭제 (관리자)
  - 현재 세션 주문 조회
  - 주문 데이터 검증
- **인터페이스**: REST API (`/api/stores/{storeId}/orders/*`)

### 6. OrderHistoryComponent
- **목적**: 과거 주문 이력 관리
- **책임**:
  - 세션 종료 시 주문 이력 저장
  - 테이블별 과거 주문 조회
  - 날짜 필터링
- **인터페이스**: REST API (`/api/stores/{storeId}/order-history/*`)

### 7. SSEComponent
- **목적**: 실시간 이벤트 전송
- **책임**:
  - SSE 연결 관리 (관리자 대시보드)
  - 신규 주문 이벤트 발행
  - 주문 상태 변경 이벤트 발행
  - 매장별 이벤트 격리
- **인터페이스**: SSE endpoint (`/api/stores/{storeId}/events`)

### 8. CustomerUI
- **목적**: 고객용 웹 인터페이스
- **책임**:
  - 자동 로그인 (localStorage)
  - 메뉴 조회/카테고리 탐색
  - 장바구니 관리 (localStorage)
  - 주문 생성 (요청사항, 맵기 옵션)
  - 주문 내역 조회
- **기술**: Vanilla HTML/CSS/JavaScript

### 9. AdminUI
- **목적**: 관리자용 웹 인터페이스
- **책임**:
  - 관리자 로그인
  - 실시간 주문 모니터링 (SSE 수신)
  - 주문 상태 변경
  - 테이블 관리 (초기 설정, 이용 완료)
  - 과거 주문 내역 조회
  - 메뉴 관리 (Could)
- **기술**: Vanilla HTML/CSS/JavaScript
