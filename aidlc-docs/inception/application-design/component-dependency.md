# Application Design - Component Dependencies

## 의존성 매트릭스

| Component | 의존 대상 | 통신 방식 |
|-----------|-----------|-----------|
| CustomerUI | AuthComponent, MenuComponent, OrderComponent | REST API (HTTP) |
| AdminUI | AuthComponent, MenuComponent, OrderComponent, TableComponent, OrderHistoryComponent, SSEComponent | REST API + SSE |
| OrderService | OrderComponent, TableComponent, SSEComponent | 내부 메서드 호출 |
| TableSessionService | OrderHistoryComponent, TableComponent, SSEComponent | 내부 메서드 호출 |
| AuthService | StoreComponent, AuthComponent | 내부 메서드 호출 |
| OrderComponent | (독립) | - |
| MenuComponent | (독립) | - |
| TableComponent | (독립) | - |
| OrderHistoryComponent | (독립) | - |
| SSEComponent | (독립) | - |
| StoreComponent | (독립) | - |

## 데이터 흐름

```
[Customer UI] --HTTP--> [Spring Boot API]
                            |
                  +---------+---------+
                  |         |         |
              AuthService  OrderService  TableSessionService
                  |         |    |         |       |
              AuthComp  OrderComp TableComp  OrderHistoryComp
                  |                |
              StoreComp        SSEComp --SSE--> [Admin UI]
```

## 통신 패턴

| 패턴 | 사용처 | 설명 |
|------|--------|------|
| REST (동기) | UI ↔ API | 모든 CRUD 요청 |
| SSE (단방향 스트림) | API → Admin UI | 실시간 주문/상태 이벤트 |
| 내부 호출 (동기) | Service → Component | 서비스 레이어 오케스트레이션 |

## 계층 구조

```
[Presentation]  CustomerUI, AdminUI
      |
[API Layer]     REST Controllers (Spring MVC)
      |
[Service Layer] OrderService, TableSessionService, AuthService
      |
[Component]     Auth, Store, Menu, Table, Order, OrderHistory, SSE
      |
[Data Layer]    JPA Repositories → PostgreSQL
```
