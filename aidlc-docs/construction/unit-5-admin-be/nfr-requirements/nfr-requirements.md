# Unit 5: Admin Backend - NFR Requirements

## NFR-ADM-01: 응답 시간
- 대시보드 API: 2초 이내 (전체 테이블 + 주문 데이터 조합)
- 주문 상태 변경/삭제: 1초 이내
- 세션 종료: 3초 이내 (스냅샷 생성 + 원본 삭제 트랜잭션)
- 메뉴 CRUD: 1초 이내
- SSE 이벤트 전달: 2초 이내

## NFR-ADM-02: SSE 연결 관리
- SseEmitter timeout: 30분
- Heartbeat: 30초 간격
- 연결 끊김 시 자동 정리
- 매장별 emitter 격리

## NFR-ADM-03: 트랜잭션 원자성
- 세션 종료: 스냅샷 생성 + 원본 삭제 + 세션 비활성화 = 단일 @Transactional
- 주문 삭제: OrderItem + Order 삭제 = 단일 @Transactional
- 실패 시 전체 롤백

## NFR-ADM-04: SSE Fire-and-Forget
- SSE 발행 실패 시 비즈니스 로직에 영향 없음
- 실패 시 로그만 남김
- Unit 6 (Admin FE)의 주기적 polling이 안전망

## NFR-ADM-05: 보안
- 모든 API는 ADMIN 역할 필수 (Unit 2 SecurityConfig에서 처리)
- 매장 격리: URL storeId vs JWT storeId 검증 (StoreAccessFilter)
- PIN 변경 시 bcrypt 해싱
