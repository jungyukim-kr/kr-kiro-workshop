# Unit 3: Customer Backend - Code Generation Summary

## TDD Execution Results
- **Total tests**: 25
- **Passed**: 25
- **Failed**: 0
- **Approach**: TDD (RED-GREEN-REFACTOR)

## Generated Files

### Application Code (`backend/src/main/java/com/tableorder/`)

| File | Layer | Description |
|------|-------|-------------|
| `customer/exception/CustomerException.java` | Exception | 커스텀 예외 (errorCode, httpStatus) |
| `customer/exception/CustomerExceptionHandler.java` | Exception | @RestControllerAdvice 에러 핸들러 |
| `customer/dto/CategoriesResponse.java` | DTO | 카테고리 목록 응답 |
| `customer/dto/MenuDto.java` | DTO | 메뉴 정보 (hasSpicyOptions 포함) |
| `customer/dto/MenuListResponse.java` | DTO | 메뉴 목록 응답 |
| `customer/dto/SpicyOptionDto.java` | DTO | 맵기 옵션 |
| `customer/dto/SpicyOptionsResponse.java` | DTO | 맵기 옵션 목록 응답 |
| `customer/dto/CreateOrderRequest.java` | DTO | 주문 생성 요청 (@Valid) |
| `customer/dto/OrderItemRequest.java` | DTO | 주문 항목 요청 |
| `customer/dto/OrderItemDto.java` | DTO | 주문 항목 응답 (스냅샷) |
| `customer/dto/OrderResponse.java` | DTO | 주문 응답 |
| `customer/dto/OrderListResponse.java` | DTO | 주문 목록 + 페이지네이션 응답 |
| `customer/repository/MenuRepository.java` | Repository | 메뉴 조회 (카테고리 DISTINCT, 카테고리별) |
| `customer/repository/MenuSpicyOptionRepository.java` | Repository | 맵기 옵션 조회 |
| `customer/repository/CustomerTableSessionRepository.java` | Repository | 세션 조회/생성 |
| `customer/repository/OrderRepository.java` | Repository | 주문 조회/생성, 주문번호 카운트 |
| `customer/repository/OrderItemRepository.java` | Repository | 주문 항목 조회/생성 |
| `customer/service/CustomerMenuService.java` | Service | 카테고리/메뉴/맵기옵션 조회 |
| `customer/service/CustomerOrderService.java` | Service | 주문 생성/조회, 세션 자동 생성, SSE 발행 |
| `customer/controller/CustomerMenuController.java` | Controller | 메뉴 관련 3개 API |
| `customer/controller/CustomerOrderController.java` | Controller | 주문 관련 2개 API |
| `common/service/SseService.java` | Interface | SSE 발행 인터페이스 (Unit 5 구현) |
| `common/service/NoOpSseService.java` | Service | SSE 빈 구현체 (Unit 5 전까지 사용) |

### Modified Files
| File | Change |
|------|--------|
| `common/entity/Admin.java` | 중복 getter/setter 제거 (merge 아티팩트) |

### Test Code (`backend/src/test/java/com/tableorder/customer/`)

| File | Tests | Description |
|------|-------|-------------|
| `service/CustomerMenuServiceTest.java` | 7 | 카테고리/메뉴/맵기옵션 조회 |
| `service/CustomerOrderServiceTest.java` | 10 | 주문 생성/조회, 에러 케이스, SSE |
| `controller/CustomerMenuControllerTest.java` | 4 | 메뉴 API 엔드포인트 |
| `controller/CustomerOrderControllerTest.java` | 4 | 주문 API 엔드포인트 |

## Stories Coverage
| Story | Status |
|-------|--------|
| US-C02 (메뉴 조회) | ✅ TC-CUST-001~004, 018~019 |
| US-C05 (주문하기) | ✅ TC-CUST-005~015, 020~024 |
| US-C06 (주문 내역) | ✅ TC-CUST-016, 025 |
| US-C07 (주문 상태) | ✅ TC-CUST-016, 025 |
| US-C08 (빈 세션) | ✅ TC-CUST-017 |
