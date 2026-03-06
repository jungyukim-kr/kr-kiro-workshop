# Unit 5: Admin Backend - NFR Design Patterns

## 1. SSE 실시간 이벤트 패턴

### 적용 대상
관리자 대시보드 실시간 업데이트

### 구현 방식
```
SseService (실제 구현 — NoOpSseService 대체):
  - ConcurrentHashMap<Long, List<SseEmitter>> emitters (storeId별)
  - subscribe(storeId) → SseEmitter
  - publish(storeId, eventType, data) → void (fire-and-forget)
  - heartbeat: @Scheduled 30초 간격
  - onCompletion/onTimeout/onError → emitter 제거
```

## 2. 트랜잭션 원자성 패턴

### 적용 대상
세션 종료 (스냅샷 + 삭제), 주문 삭제

### 구현 방식
```
@Transactional
closeSession(tableId):
  1. 스냅샷 생성 (OrderHistory + OrderHistoryItem)
  2. 원본 삭제 (OrderItem → Order)
  3. 세션 비활성화
  // SSE 발행은 트랜잭션 커밋 후
```

## 3. 계층 분리 패턴

### 구현 방식
```
Controller (요청/응답, @Valid)
  ↓ DTO
Service (비즈니스 로직, @Transactional)
  ↓ Entity
Repository (JPA)
```

| 계층 | 컴포넌트 |
|------|----------|
| Controller | AdminDashboardController, AdminOrderController, AdminTableController, AdminMenuController, SseController |
| Service | AdminDashboardService, AdminOrderService, AdminTableService, AdminMenuService, SseService(구현) |
| Repository | 7개 Repository |

## 4. 통합 에러 처리 패턴

### 구현 방식
```
@RestControllerAdvice(basePackages = "com.tableorder.admin")
AdminExceptionHandler:
  handleAdminException → 에러코드별 HTTP 상태
```
- Unit 2 ErrorResponse 형식 재사용
- Unit 3 CustomerException과 동일 패턴 (AdminException)
