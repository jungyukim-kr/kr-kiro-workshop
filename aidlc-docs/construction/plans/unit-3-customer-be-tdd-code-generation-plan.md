# TDD Code Generation Plan for Unit 3: Customer Backend

## Unit Context
- **Workspace Root**: /Users/jungyukim/Workspace/kr-kiro-workshop
- **Project Type**: Greenfield (monolith, 기존 코드 존재)
- **Stories**: US-C02, US-C05, US-C06, US-C07, US-C08
- **Dependencies**: Unit 1 (엔티티), Unit 2 (인증/보안, ErrorResponse, AuthInfo)

## Code Location
- **Application**: `backend/src/main/java/com/tableorder/customer/`
- **Tests**: `backend/src/test/java/com/tableorder/customer/`
- **Shared**: `backend/src/main/java/com/tableorder/common/` (기존 엔티티 재사용)

---

### Plan Step 0: Contract Skeleton + DTO + Exception + Repository 생성
- [x] Exception: `customer/exception/CustomerException.java`
- [x] Exception Handler: `customer/exception/CustomerExceptionHandler.java`
- [x] DTOs: CreateOrderRequest, OrderItemRequest, CategoriesResponse, MenuListResponse, MenuDto, SpicyOptionsResponse, SpicyOptionDto, OrderResponse, OrderItemDto, OrderListResponse
- [x] Repositories: MenuRepository, MenuSpicyOptionRepository, CustomerTableSessionRepository, OrderRepository, OrderItemRepository
- [x] SSE Interface: `common/service/SseService.java` (interface)
- [x] SSE NoOp: `common/service/NoOpSseService.java` (기본 구현)
- [x] Service Skeletons: CustomerMenuService, CustomerOrderService (메서드 stub)
- [x] Controller Skeletons: CustomerMenuController, CustomerOrderController (메서드 stub)
- [x] 컴파일 확인

### Plan Step 1: CustomerMenuService TDD (TC-CUST-001 ~ TC-CUST-007)
- [x] getCategories() — RED/GREEN/REFACTOR (TC-CUST-001, TC-CUST-002)
- [x] getMenusByCategory() — RED/GREEN/REFACTOR (TC-CUST-003, TC-CUST-004)
- [x] getSpicyOptions() — RED/GREEN/REFACTOR (TC-CUST-005, TC-CUST-006, TC-CUST-007)
- [x] 전체 테스트 실행 확인

### Plan Step 2: CustomerOrderService TDD (TC-CUST-008 ~ TC-CUST-017)
- [x] createOrder() 정상 케이스 — RED/GREEN/REFACTOR (TC-CUST-008, TC-CUST-009)
- [x] createOrder() 에러 케이스 — RED/GREEN/REFACTOR (TC-CUST-010 ~ TC-CUST-013)
- [x] createOrder() 주문번호 채번 + SSE — RED/GREEN/REFACTOR (TC-CUST-014, TC-CUST-015)
- [x] getOrders() — RED/GREEN/REFACTOR (TC-CUST-016, TC-CUST-017)
- [x] 전체 테스트 실행 확인

### Plan Step 3: Controller Layer TDD (TC-CUST-018 ~ TC-CUST-025)
- [x] CustomerMenuController — RED/GREEN/REFACTOR (TC-CUST-018 ~ TC-CUST-021)
- [x] CustomerOrderController — RED/GREEN/REFACTOR (TC-CUST-022 ~ TC-CUST-025)
- [x] 전체 테스트 실행 확인 (Unit 2 + Unit 3 모든 테스트)

### Plan Step 4: Documentation + Final Verification
- [x] code-summary.md 생성
- [x] aidlc-state.md 업데이트
- [x] 전체 빌드 확인 (gradle build)
