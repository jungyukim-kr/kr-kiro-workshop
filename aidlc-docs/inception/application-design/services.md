# Application Design - Services

## Service Layer 개요

Service Layer는 컴포넌트 간 오케스트레이션을 담당합니다.

## OrderService (핵심 서비스)

**책임**: 주문 생성 시 여러 컴포넌트를 조율

**오케스트레이션 흐름 - 주문 생성:**
1. OrderComponent.createOrder() → 주문 저장
2. TableComponent.startSession() → 세션 없으면 자동 시작
3. SSEComponent.publishNewOrder() → 관리자에게 실시간 알림

**오케스트레이션 흐름 - 주문 상태 변경:**
1. OrderComponent.updateOrderStatus() → 상태 업데이트
2. SSEComponent.publishStatusChange() → 관리자/고객에게 알림

**오케스트레이션 흐름 - 주문 삭제:**
1. OrderComponent.deleteOrder() → 주문 삭제
2. SSEComponent.publishStatusChange() → 관리자에게 알림

## TableSessionService

**책임**: 테이블 세션 라이프사이클 관리

**오케스트레이션 흐름 - 이용 완료:**
1. OrderHistoryComponent.archiveSessionOrders() → 주문 이력 저장
2. TableComponent.endSession() → 세션 종료, 주문 목록/총액 리셋
3. SSEComponent.publishStatusChange() → 관리자에게 알림

## AuthService

**책임**: 인증 흐름 관리

**오케스트레이션 흐름 - 관리자 로그인:**
1. StoreComponent.getStore() → 매장 존재 확인
2. AuthComponent.adminLogin() → 인증 및 JWT 발급

**오케스트레이션 흐름 - 테이블 로그인:**
1. StoreComponent.getStore() → 매장 존재 확인
2. AuthComponent.tableLogin() → 테이블 인증 및 JWT 발급
