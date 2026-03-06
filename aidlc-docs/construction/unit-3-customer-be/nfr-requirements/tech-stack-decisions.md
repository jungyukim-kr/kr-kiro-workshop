# Unit 3: Customer Backend - Tech Stack Decisions

## Unit 3 추가 기술 결정

| 영역 | 기술 | 선택 근거 |
|------|------|-----------|
| REST Controller | Spring Web (@RestController) | Unit 1에서 확정 |
| 트랜잭션 | @Transactional (Spring) | 세션+주문 원자성 보장 |
| 페이지네이션 | Spring Data Pageable | 오프셋 기반, 표준 지원 |
| DTO 변환 | 수동 매핑 (생성자/정적 팩토리) | 외부 라이브러리 불필요 |
| 검증 | Jakarta Validation (@Valid) | 요청 DTO 검증 |
| SSE 연동 | SseEmitter (Spring Web) | Unit 5에서 구현, Unit 3에서 호출 |

## 다른 팀 전달 사항

### Unit 6 (Admin FE) — 주기적 refresh 필요
- SSE 연결 끊김 또는 이벤트 누락 대비
- 관리자 대시보드에서 주기적으로 주문 목록 polling (30초~1분 권장)
- SSE 재연결 로직도 포함 필요
