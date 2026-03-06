# Unit 3: Customer Backend - Business Rules

## BR-CUST-01: 메뉴 조회 규칙

### BR-CUST-01.1: 카테고리 조회
- 해당 매장(storeId)에 등록된 메뉴의 카테고리만 반환
- 메뉴가 없는 카테고리는 반환하지 않음
- 정렬: 카테고리명 알파벳순 (한글 가나다순)

### BR-CUST-01.2: 메뉴 목록 조회
- 해당 매장 + 해당 카테고리의 메뉴만 반환
- display_order 기준 오름차순 정렬
- spicyLevel 포함, spicyOptions는 별도 API
- `hasSpicyOptions` 플래그로 맵기 옵션 존재 여부 표시

### BR-CUST-01.3: 맵기 옵션 조회
- 메뉴가 해당 매장 소속인지 확인
- display_order 기준 오름차순 정렬

---

## BR-CUST-02: 주문 생성 규칙

### BR-CUST-02.1: 주문 항목 검증
- items 배열은 1개 이상 필수
- 각 item의 menuId는 해당 매장에 존재하는 메뉴여야 함
- quantity > 0 필수
- unitPrice는 DB의 Menu.price와 일치해야 함 → 불일치 시 PRICE_MISMATCH 에러

### BR-CUST-02.2: 맵기 옵션 검증
- spicyOption이 지정된 경우, 해당 메뉴의 MenuSpicyOption에 등록된 option_name이어야 함
- 미등록 옵션이면 INVALID_SPICY_OPTION 에러
- spicyOption이 null이면 검증 스킵 (맵기 옵션 없는 메뉴 또는 미선택)

### BR-CUST-02.3: 요청사항
- specialRequest는 선택 사항 (null 허용)
- 최대 길이: TEXT (제한 없음, 단 프론트에서 200자 권장)

### BR-CUST-02.4: 세션 자동 생성
- JWT의 sessionId가 null이면 새 세션 생성
  - sessionCode: UUID 생성
  - active: true
  - storeId, tableId: JWT에서 추출
- JWT의 sessionId가 있으면 해당 세션의 active 여부 확인
  - active=false이면 SESSION_NOT_FOUND 에러

### BR-CUST-02.5: 주문 번호 채번
- 형식: `{yyyyMMdd}-{3자리 순번}` (예: 20260306-001)
- 매장별 + 당일 기준 순번
- 동시 주문 시 중복 방지: DB 조회 후 +1 (낙관적 접근, 매장 규모에서 충돌 확률 극히 낮음)

### BR-CUST-02.6: 금액 계산
- totalAmount = Σ(item.unitPrice × item.quantity)
- 서버에서 재계산하여 저장

### BR-CUST-02.7: 스냅샷 저장
- OrderItem에 menuName, unitPrice를 주문 시점 값으로 저장
- menuName은 DB에서 조회한 현재 메뉴명 사용

### BR-CUST-02.8: SSE 이벤트 발행
- 주문 생성 성공 시 해당 매장의 SSE 구독자에게 NEW_ORDER 이벤트 발행
- Unit 5 (Admin BE)의 SSE 컴포넌트 호출

---

## BR-CUST-03: 주문 내역 조회 규칙

### BR-CUST-03.1: 세션 기반 조회
- JWT의 sessionId 기반으로 현재 세션의 주문만 조회
- sessionId가 null이면 빈 목록 반환 (에러 아님)

### BR-CUST-03.2: 정렬
- 주문 생성 시각 기준 내림차순 (최신 주문이 위)

### BR-CUST-03.3: 페이지네이션
- 오프셋 기반: page (0부터), size (기본 10)
- 응답에 totalElements, totalPages 포함

### BR-CUST-03.4: 주문 상태 표시
- 각 주문의 status 필드로 상태 표시 (WAITING/PREPARING/DONE)
- 상태 변경은 관리자만 가능 (Unit 5)

---

## BR-CUST-04: 가격 불일치 에러 연계

### BR-CUST-04.1: 고객 측 처리
- PRICE_MISMATCH 에러 시 변경된 메뉴 정보를 에러 응답에 포함
- 프론트엔드에서 장바구니 갱신 유도

### BR-CUST-04.2: 관리자 측 연계 (Unit 5)
- 관리자가 메뉴 가격 변경 시 SSE로 MENU_UPDATED 이벤트 발행
- 고객 화면에서 메뉴 목록 자동 갱신 (Unit 4에서 처리)

---

## 에러 코드 정의

| Error Code | HTTP Status | 설명 |
|------------|-------------|------|
| MENU_NOT_FOUND | 404 | 메뉴 미존재 |
| PRICE_MISMATCH | 409 | 메뉴 가격 변경됨 |
| INVALID_SPICY_OPTION | 400 | 미등록 맵기 옵션 |
| SESSION_NOT_FOUND | 404 | 세션 미존재 또는 비활성 |
| VALIDATION_FAILED | 400 | 입력 검증 실패 |
