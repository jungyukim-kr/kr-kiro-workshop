# Unit 3: Customer Backend - Functional Design Plan

## Unit Context
- **담당 스토리**: US-C02, US-C05, US-C06, US-C07, US-C08
- **범위**: 고객용 메뉴 조회 API, 주문 생성 API, 주문 내역 조회 API
- **의존**: Unit 1 (엔티티/스키마), Unit 2 (인증/JWT 필터)
- **URL 패턴**: `/api/stores/{storeId}/customer/**` (역할: TABLE)

## Plan Steps

- [x] Step 1: 도메인 엔티티 분석 (Unit 1 엔티티 중 Unit 3에서 사용하는 것)
- [x] Step 2: API 엔드포인트 설계 (URL, Method, Request/Response)
- [x] Step 3: 비즈니스 로직 모델 (주문 생성 플로우, 세션 자동 생성, 주문번호 채번)
- [x] Step 4: 비즈니스 규칙 정의 (검증, 제약조건, 에러 처리)
- [x] Step 5: 산출물 생성 (domain-entities.md, business-logic-model.md, business-rules.md)

## Questions

### 비즈니스 로직

**Q1. 메뉴 조회 API 구조**
메뉴 조회 시 카테고리 목록과 메뉴 목록을 어떻게 제공할까요?

A) 단일 API — `/customer/menus`로 전체 메뉴 + 카테고리 목록을 한번에 반환 (프론트에서 카테고리별 필터링)
B) 분리 API — `/customer/categories`와 `/customer/menus?category=찌개`를 별도 제공
C) 단일 API + 카테고리 파라미터 — `/customer/menus?category=찌개` (카테고리 미지정 시 전체 반환)

[Answer]: B

**Q2. 맵기 옵션 포함 방식**
메뉴 조회 시 맵기 옵션(MenuSpicyOption)을 어떻게 포함할까요?

A) 메뉴 목록에 항상 포함 — 각 메뉴 객체 안에 `spicyOptions: [...]` 배열 포함
B) 별도 API — `/customer/menus/{menuId}/spicy-options`로 필요할 때만 조회
C) 주문 확인 화면 진입 시에만 조회 — 장바구니에 담긴 메뉴의 옵션만 별도 요청

[Answer]: B

**Q3. 주문 생성 시 세션 자동 생성**
US-A07에 따르면 첫 주문 시 세션이 자동 생성됩니다. JWT 토큰의 sessionId가 null인 경우의 처리:

A) 주문 생성 API 내에서 세션 생성 → 주문 생성을 한 트랜잭션으로 처리
B) 주문 전에 별도 세션 생성 API를 호출하고, 주문 생성은 sessionId 필수로 요구

[Answer]: A는 기존 세션이 유지되어야 할때도 주문할 때 API가 세션을 강제생성하는게 걱정되고, B는 세션이 생성되기 전에 세션을 생성해줄 무언가가 없는게 걱정되는데? 너가 좋은 방법을 추천해줘. 

**Q4. 주문 번호 형식**
주문 번호 채번 방식:

A) 매장별 일련번호 — `#1`, `#2`, `#3` (당일 기준 리셋 없이 계속 증가)
B) 날짜 포함 — `20260306-001`, `20260306-002` (당일 기준 리셋)
C) 매장코드 포함 — `STORE1-0042`

[Answer]: 가급적이면 DB 컬럼 속성을 바꾸지 않고 하고 싶은데, 그러려면 A밖에 대안이 없는거 아냐? C가 가장 좋은 옵션이긴 해. 

### 데이터 플로우

**Q5. 주문 내역 조회 범위**
US-C06에서 "현재 세션의 주문만 표시"인데, 세션이 없는 경우(아직 주문 안 한 테이블):

A) 빈 목록 반환
B) 에러 반환 (세션 없음)

[Answer]: A

**Q6. 페이지네이션 방식 (US-C08)**

A) 오프셋 기반 — `?page=0&size=10`
B) 커서 기반 — `?cursor=lastOrderId&size=10`

[Answer]: A

### 에러 처리

**Q7. 주문 생성 시 메뉴 존재 여부 검증**
장바구니의 메뉴가 서버에서 삭제되었거나 가격이 변경된 경우:

A) 주문 시점의 DB 가격으로 주문 생성 (클라이언트 가격 무시)
B) 클라이언트가 보낸 가격과 DB 가격 비교 → 불일치 시 에러 반환
C) 클라이언트 가격으로 주문 생성 (DB 가격 무시)

[Answer]: C

**Q8. 맵기 옵션 검증**
주문 시 선택한 맵기 옵션이 해당 메뉴에 등록되지 않은 옵션인 경우:

A) 에러 반환 (유효하지 않은 옵션)
B) 무시하고 주문 생성 (옵션 null 처리)

[Answer]: A (이건 Frontend에서 메뉴에 등록되지 않은 맵기 옵션을 선택할 수 없도록 하는게 맞을 것 같아.)
