# Unit 5: Admin Backend - Business Rules

## BR-ADM-01: 주문 모니터링 규칙

### BR-ADM-01.1: 대시보드 데이터 범위
- 해당 매장(storeId)의 모든 테이블 표시
- 활성 세션이 있는 테이블만 주문 데이터 포함
- 활성 세션이 없는 테이블은 session=null
- 주문은 createdAt 내림차순 (최신 주문이 위)

### BR-ADM-01.2: 테이블별 총 주문액
- session.totalAmount = Σ(해당 세션의 모든 Order.totalAmount)
- 주문 삭제 시 재계산 필요 (프론트에서 SSE 이벤트 기반 처리)

---

## BR-ADM-02: 주문 상태 변경 규칙

### BR-ADM-02.1: 상태 전이 (단방향)
- WAITING → PREPARING → DONE
- 허용되지 않는 전이 시 INVALID_STATUS_TRANSITION 에러
- 이미 같은 상태로 변경 시도 시에도 에러

### BR-ADM-02.2: 상태 전이 매트릭스
| 현재 상태 | WAITING | PREPARING | DONE |
|-----------|---------|-----------|------|
| WAITING | ✗ | ✓ | ✗ |
| PREPARING | ✗ | ✗ | ✓ |
| DONE | ✗ | ✗ | ✗ |

### BR-ADM-02.3: SSE 연계
- 상태 변경 성공 시 ORDER_STATUS_CHANGED 이벤트 발행
- 고객 화면에서도 주문 상태 갱신 가능 (Unit 4에서 polling 또는 SSE 구독)

---

## BR-ADM-03: 주문 삭제 규칙

### BR-ADM-03.1: Hard Delete
- Order + OrderItem 모두 DB에서 물리 삭제
- FK 제약조건: OrderItem 먼저 삭제 → Order 삭제

### BR-ADM-03.2: 삭제 제한
- 해당 매장 소속 주문만 삭제 가능 (storeId 검증)
- 존재하지 않는 주문 삭제 시 ORDER_NOT_FOUND

### BR-ADM-03.3: SSE 연계
- 삭제 성공 시 ORDER_DELETED 이벤트 발행

---

## BR-ADM-04: 테이블 PIN 변경 규칙

### BR-ADM-04.1: PIN 형식
- 정확히 4자리 숫자 (^\d{4}$)
- bcrypt로 해싱하여 저장

### BR-ADM-04.2: 접근 제어
- ADMIN 역할만 변경 가능
- 해당 매장 소속 테이블만 변경 가능

---

## BR-ADM-05: 세션 종료 규칙 (이용 완료)

### BR-ADM-05.1: 스냅샷 생성
- OrderHistory 1건 생성 (세션 요약)
- OrderHistoryItem N건 생성 (각 주문의 각 항목)
- 원본 데이터에서 복사할 필드:
  - OrderHistory: storeId, tableId, sessionId, sessionCode, totalAmount, orderCount, startedAt, endedAt
  - OrderHistoryItem: orderHistoryId, orderNumber, menuName, quantity, unitPrice, spicyOption, specialRequest, orderedAt, orderStatus

### BR-ADM-05.2: 원본 정리
- OrderItem 전체 삭제 (해당 세션의 모든 주문)
- Order 전체 삭제 (해당 세션)
- TableSession.active = false, endedAt = now()

### BR-ADM-05.3: 트랜잭션 원자성
- 스냅샷 생성 + 원본 삭제 + 세션 비활성화 = 단일 @Transactional
- 어느 단계에서든 실패 시 전체 롤백

### BR-ADM-05.4: 빈 세션 종료
- 주문이 없는 세션도 종료 가능
- OrderHistory는 totalAmount=0, orderCount=0으로 생성

---

## BR-ADM-06: 과거 이력 조회 규칙

### BR-ADM-06.1: 조회 범위
- 특정 테이블의 과거 세션 목록 (endedAt 내림차순)
- 오프셋 페이지네이션 (page, size)

### BR-ADM-06.2: 이력 상세
- 각 OrderHistory에 대해 OrderHistoryItem 목록 포함
- 주문 시점의 스냅샷 데이터 (메뉴명, 가격 등)

---

## BR-ADM-07: 메뉴 CRUD 규칙

### BR-ADM-07.1: 메뉴 등록
- 필수: name, price, category
- 선택: description, imageUrl, spicyLevel, displayOrder
- price > 0 검증
- 해당 매장(storeId)에 등록

### BR-ADM-07.2: 메뉴 수정
- 해당 매장 소속 메뉴만 수정 가능
- 가격 변경 시 SSE MENU_UPDATED 이벤트 발행
- 존재하지 않는 메뉴 수정 시 MENU_NOT_FOUND

### BR-ADM-07.3: 메뉴 삭제
- 해당 매장 소속 메뉴만 삭제 가능
- Hard Delete (맵기 옵션 관리 스킵이므로 FK 이슈 주의 — MenuSpicyOption도 함께 삭제)
- SSE MENU_UPDATED 이벤트 발행

---

## BR-ADM-08: SSE 규칙

### BR-ADM-08.1: 연결 관리
- SseEmitter timeout: 30분
- heartbeat: 30초 간격으로 빈 comment 전송
- 연결 끊김 시 emitter 목록에서 제거

### BR-ADM-08.2: 매장 격리
- storeId별로 emitter 관리
- 이벤트는 해당 매장의 emitter에만 전송

### BR-ADM-08.3: 안전망
- SSE 발행 실패 시 로그만 남김 (fire-and-forget)
- Unit 6 (Admin FE)에서 30초~1분 주기 polling으로 보완

---

## 에러 코드 정의

| Error Code | HTTP Status | 설명 |
|------------|-------------|------|
| ORDER_NOT_FOUND | 404 | 주문 미존재 |
| INVALID_STATUS_TRANSITION | 400 | 허용되지 않는 상태 전이 |
| TABLE_NOT_FOUND | 404 | 테이블 미존재 |
| SESSION_NOT_FOUND | 404 | 활성 세션 미존재 |
| MENU_NOT_FOUND | 404 | 메뉴 미존재 |
| VALIDATION_FAILED | 400 | 입력 검증 실패 |
