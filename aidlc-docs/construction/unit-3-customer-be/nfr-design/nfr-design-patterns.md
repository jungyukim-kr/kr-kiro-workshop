# Unit 3: Customer Backend - NFR Design Patterns

## 1. 낙관적 재시도 패턴 (Optimistic Retry)

### 적용 대상
주문 번호 채번 시 동시성 충돌 처리

### 구현 방식
```
generateOrderNumber(storeId):
  datePrefix = LocalDate.now().format("yyyyMMdd")
  
  for (attempt = 1; attempt <= 3; attempt++):
    count = orderRepository.countByStoreIdAndOrderNumberStartingWith(storeId, datePrefix)
    orderNumber = datePrefix + "-" + String.format("%03d", count + 1)
    
    try:
      order.setOrderNumber(orderNumber)
      orderRepository.save(order)
      return orderNumber
    catch (DataIntegrityViolationException):  // UNIQUE 위반
      if (attempt == 3): throw OrderNumberGenerationException
      continue
```

### 설계 근거
- DB Lock 없이 동시성 처리
- order_number UNIQUE 제약조건이 자연스러운 충돌 감지 역할
- 매장 규모(수십 테이블)에서 재시도 발생 확률 극히 낮음
- 최대 3회 재시도로 충분

---

## 2. 트랜잭션 원자성 패턴

### 적용 대상
세션 자동 생성 + 주문 생성

### 구현 방식
```
@Transactional
createOrder(storeId, tableId, sessionId, orderRequest):
  // 1. 세션 처리 (필요 시 생성)
  if (sessionId == null):
    session = createNewSession(storeId, tableId)
  else:
    session = validateActiveSession(sessionId)
  
  // 2. 주문 생성 (검증 포함)
  order = buildAndSaveOrder(...)
  
  // 3. SSE 발행 (트랜잭션 외부)
  // → 실패해도 주문은 커밋됨
```

### 설계 근거
- @Transactional로 세션+주문 원자성 보장
- 어느 단계에서든 예외 발생 시 전체 롤백
- SSE 발행은 트랜잭션 커밋 후 별도 처리

---

## 3. SSE Fire-and-Forget 패턴

### 적용 대상
주문 생성 후 관리자 알림

### 구현 방식
```
@Transactional
createOrder(...):
  order = saveOrder(...)
  return order

// Controller 또는 @TransactionalEventListener에서
afterOrderCreated(order):
  try:
    sseService.publishNewOrder(storeId, orderDTO)
  catch (Exception e):
    log.warn("SSE 발행 실패: {}", e.getMessage())
    // 무시 — 관리자 화면 주기적 refresh로 보완
```

### 설계 근거
- 주문 성공이 SSE 발행에 의존하지 않음
- SSE 실패 시 로그만 남기고 주문은 정상 완료
- 관리자 화면의 주기적 polling이 안전망 역할

---

## 4. 계층 분리 패턴 (Layered Architecture)

### 적용 대상
Unit 3 전체 코드 구조

### 구현 방식
```
Controller (요청/응답)
  ↓ DTO
Service (비즈니스 로직)
  ↓ Entity
Repository (데이터 접근)
```

| 계층 | 책임 | 의존 |
|------|------|------|
| Controller | @Valid 검증, DTO 변환, HTTP 상태 코드 | Service |
| Service | 비즈니스 규칙, 트랜잭션, 에러 처리 | Repository |
| Repository | JPA 쿼리, 페이지네이션 | Entity |

### 설계 근거
- 관심사 분리로 유지보수성 확보
- 각 계층 독립적 테스트 가능
- DTO로 엔티티 직접 노출 방지

---

## 5. 통합 에러 처리 패턴

### 적용 대상
Unit 2 에러 형식과 통일

### 구현 방식
```
@RestControllerAdvice(basePackages = "com.tableorder.customer")
CustomerExceptionHandler:
  
  handleMenuNotFoundException → 404, MENU_NOT_FOUND
  handlePriceMismatchException → 409, PRICE_MISMATCH
  handleInvalidSpicyOptionException → 400, INVALID_SPICY_OPTION
  handleSessionNotFoundException → 404, SESSION_NOT_FOUND
  handleValidationException → 400, VALIDATION_FAILED
```

### 응답 형식 (Unit 2와 동일)
```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 메시지",
  "timestamp": "2026-03-06T15:30:00+09:00"
}
```
