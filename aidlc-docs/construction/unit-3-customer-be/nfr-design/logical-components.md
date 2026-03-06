# Unit 3: Customer Backend - Logical Components

## 컴포넌트 구성

```
┌──────────────────────────────────────────────────────┐
│                  Spring Boot App                     │
│                                                      │
│  ┌─────────────────── Unit 2 (Auth) ──────────────┐  │
│  │ JwtAuthenticationFilter → StoreAccessFilter     │  │
│  └─────────────────────────────────────────────────┘  │
│                        ↓                             │
│  ┌─────────────────── Unit 3 (Customer BE) ───────┐  │
│  │                                                 │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │           Controllers                    │   │  │
│  │  │  CustomerMenuController                  │   │  │
│  │  │  CustomerOrderController                 │   │  │
│  │  └──────────────┬───────────────────────────┘   │  │
│  │                 ↓                               │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │           Services                       │   │  │
│  │  │  CustomerMenuService                     │   │  │
│  │  │  CustomerOrderService                    │   │  │
│  │  └──────────────┬───────────────────────────┘   │  │
│  │                 ↓                               │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │           Repositories                   │   │  │
│  │  │  MenuRepository                          │   │  │
│  │  │  MenuSpicyOptionRepository               │   │  │
│  │  │  OrderRepository                         │   │  │
│  │  │  OrderItemRepository                     │   │  │
│  │  │  TableSessionRepository                  │   │  │
│  │  └──────────────────────────────────────────┘   │  │
│  │                                                 │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │  CustomerExceptionHandler                │   │  │
│  │  │  (@RestControllerAdvice)                 │   │  │
│  │  └──────────────────────────────────────────┘   │  │
│  │                                                 │  │
│  └─────────────────────────────────────────────────┘  │
│                        ↓ (SSE 호출)                   │
│  ┌─────────────────── Unit 5 (Admin BE) ──────────┐  │
│  │  SseService.publishNewOrder()                   │  │
│  └─────────────────────────────────────────────────┘  │
│                                                      │
└──────────────────────────────────────────────────────┘
```

## 컴포넌트 상세

| 컴포넌트 | 역할 | 의존 |
|----------|------|------|
| CustomerMenuController | 카테고리/메뉴/맵기옵션 조회 API | CustomerMenuService |
| CustomerOrderController | 주문 생성/내역 조회 API | CustomerOrderService |
| CustomerMenuService | 메뉴 조회 비즈니스 로직 | MenuRepository, MenuSpicyOptionRepository |
| CustomerOrderService | 주문 생성/조회 비즈니스 로직 | OrderRepository, OrderItemRepository, TableSessionRepository, MenuRepository, MenuSpicyOptionRepository |
| CustomerExceptionHandler | 비즈니스 예외 → 에러 응답 변환 | - |

## Unit 간 의존성

| 의존 방향 | 내용 |
|-----------|------|
| Unit 3 → Unit 1 | 공통 엔티티 (Menu, Order, OrderItem, TableSession 등) |
| Unit 3 → Unit 2 | JWT 인증 필터 (요청 전 자동 처리) |
| Unit 3 → Unit 5 | SSE 이벤트 발행 (SseService 호출) |
