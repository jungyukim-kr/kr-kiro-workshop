# Unit 5: Admin Backend - Functional Design Plan

## Unit Context
- **Unit**: unit-5-admin-be
- **범위**: 메뉴 CRUD API, 주문 모니터링 API, 주문 상태 변경/삭제 API, 테이블 관리 API, 세션 관리 API, 과거 이력 API, SSE 이벤트
- **Stories**: US-A02, US-A03, US-A05, US-A06, US-A07, US-A08, US-A09, US-A10, US-A11
- **Dependencies**: Unit 1 (엔티티), Unit 2 (인증), Unit 3 (SseService 인터페이스, 주문 엔티티)

## Plan Steps

- [x] Step 1: 질문 답변 수집
- [x] Step 2: Functional Design 산출물 생성 (domain-entities.md, business-logic-model.md, business-rules.md)
- [ ] Step 3: 사용자 승인

---

## Questions

Unit 5는 9개 스토리를 담당하며, 범위가 넓습니다. 설계 전 확인이 필요한 사항들입니다.

### Q1: 주문 모니터링 API 구조 (US-A02)
관리자 대시보드에서 테이블별 주문 현황을 조회하는 API 구조를 어떻게 할까요?

A) 단일 API — `GET /admin/dashboard` → 전체 테이블 + 활성 세션 + 주문 목록을 한번에 반환
B) 분리 API — `GET /admin/tables` (테이블 목록) + `GET /admin/tables/{tableId}/orders` (테이블별 주문)

추천: **A** — 대시보드 초기 로딩 시 한번의 호출로 전체 현황 파악. SSE로 이후 실시간 업데이트.

[Answer]:A

### Q2: SSE 이벤트 종류 (US-A02, US-A03, US-A06, US-A08)
관리자에게 전달할 SSE 이벤트 종류를 어떻게 정의할까요?

A) 최소 — `NEW_ORDER`, `ORDER_UPDATED` (상태변경/삭제 통합), `SESSION_CLOSED` 3개
B) 상세 — `NEW_ORDER`, `ORDER_STATUS_CHANGED`, `ORDER_DELETED`, `SESSION_CLOSED`, `MENU_UPDATED` 5개

추천: **B** — 프론트에서 이벤트별 다른 UI 처리 가능 (예: 삭제는 카드 제거, 상태변경은 색상 변경)

[Answer]:B

### Q3: 주문 삭제 방식 (US-A06)
주문 삭제 시 DB에서 어떻게 처리할까요?

A) Hard Delete — DB에서 실제 삭제 (Order + OrderItem)
B) Soft Delete — status를 "DELETED"로 변경, 조회 시 제외

추천: **A** — 세션 종료 시 OrderHistory로 스냅샷이 이미 저장되므로, 활성 주문은 hard delete가 깔끔함. 단, 세션 종료 전 삭제된 주문은 이력에 남지 않음.

[Answer]:A

### Q4: 세션 종료 시 이력 저장 범위 (US-A08)
테이블 이용 완료(세션 종료) 시 OrderHistory에 어떤 범위까지 저장할까요?

A) 전체 스냅샷 — 세션의 모든 주문 + 주문 항목을 OrderHistory/OrderHistoryItem에 복사 후, 원본 Order/OrderItem 삭제
B) 요약만 — OrderHistory에 총 금액, 주문 수만 저장, 원본 Order는 유지 (세션만 비활성화)

추천: **A** — 스냅샷 패턴으로 완전한 이력 보존. 활성 주문 테이블을 깨끗하게 유지.

[Answer]:A

### Q5: 과거 내역 조회 범위 (US-A09)
과거 내역 조회 시 어떤 단위로 조회할까요?

A) 테이블별 — `GET /admin/tables/{tableId}/history` (특정 테이블의 과거 세션 목록)
B) 매장 전체 — `GET /admin/history` (매장 전체 과거 세션, 테이블 필터 옵션)

추천: **A** — 대시보드에서 테이블 카드 클릭 → 해당 테이블 이력 조회가 자연스러운 UX 흐름

[Answer]:A

### Q6: 메뉴 관리 범위 (US-A10, US-A11)
메뉴 관리는 Could 우선순위입니다. 이번에 구현할까요?

A) 구현 — 메뉴 CRUD + 맵기 옵션 관리 + 노출 순서 조정 모두 구현
B) 스킵 — Must 스토리에 집중, 메뉴는 seed data로 유지
C) 최소 구현 — 메뉴 CRUD만 (맵기 옵션/순서 조정은 스킵)

추천: **C** — 메뉴 가격 변경이 Unit 3의 PRICE_MISMATCH 에러와 연계되므로 기본 CRUD는 있는 게 좋음

[Answer]:C

### Q7: 테이블 초기 설정 API (US-A05)
테이블 태블릿 초기 설정은 Unit 2 Auth의 테이블 로그인과 연계됩니다. 관리자가 설정하는 범위는?

A) PIN 변경만 — `PUT /admin/tables/{tableId}/pin` (테이블 PIN 변경)
B) 테이블 CRUD — 테이블 생성/수정/삭제 + PIN 설정

추천: **A** — 테이블 자체는 seed data로 이미 존재. 관리자는 PIN만 변경하면 됨.

[Answer]:A

### Q8: 주문 상태 변경 흐름 (US-A03)
상태 변경은 단방향(WAITING→PREPARING→DONE)만 허용할까요?

A) 단방향만 — 다음 단계로만 변경 가능 (되돌리기 불가)
B) 자유 변경 — 어떤 상태로든 변경 가능 (DONE→WAITING도 가능)

추천: **A** — 운영 흐름상 되돌리기가 필요하면 주문 삭제 후 재주문이 더 명확

[Answer]:A
