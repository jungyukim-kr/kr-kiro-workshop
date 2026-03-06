# Unit of Work Plan

## 분해 전략
모노레포 구조의 단일 Spring Boot 앱 + Vanilla JS 프론트엔드이므로, 논리적 모듈 단위로 분해합니다.
CONSTRUCTION 단계에서 각 unit을 순차적으로 설계/구현합니다.

## 생성 계획

### Step 1: Unit 정의
- [x] Unit 1: DB 스키마 + 공통 설정 (기반 인프라)
- [x] Unit 2: 인증 모듈 (Auth)
- [x] Unit 3: 메뉴 모듈 (Menu)
- [x] Unit 4: 주문 + 테이블 세션 모듈 (Order & Table)
- [x] Unit 5: 실시간 이벤트 모듈 (SSE)
- [x] Unit 6: 고객 프론트엔드 (Customer UI)
- [x] Unit 7: 관리자 프론트엔드 (Admin UI)

### Step 2: 산출물 생성
- [x] unit-of-work.md 생성
- [x] unit-of-work-dependency.md 생성
- [x] unit-of-work-story-map.md 생성

### Step 3: 검증
- [x] 모든 스토리가 unit에 매핑되었는지 확인
- [x] unit 간 의존성 순서 검증
