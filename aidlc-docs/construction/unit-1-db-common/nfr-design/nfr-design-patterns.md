# Unit 1: DB 스키마 + 공통 설정 - NFR Design Patterns

## 데이터 무결성 패턴

### FK 제약조건 + Application-Level Validation
- DB 레벨: FK, NOT NULL, UNIQUE, CHECK 제약조건으로 1차 방어
- Application 레벨: 서비스 레이어에서 비즈니스 규칙 검증 (상태 전이 등)
- 이중 방어로 데이터 오염 방지

### Snapshot 패턴 (OrderItem)
- 주문 항목에 menu_name, unit_price를 스냅샷으로 저장
- 메뉴 가격/이름 변경 시에도 기존 주문 기록 보존
- 감사(audit) 및 정산 정확성 보장

## 보안 패턴

### Password Hashing (bcrypt)
- cost factor 10 (기본값, ~100ms 해싱 시간)
- Admin 비밀번호, Table 비밀번호 모두 적용

### JWT Stateless Authentication
- HS256 서명, 만료 16시간
- 토큰에 store_id, role 포함
- 서버 세션 불필요 → 단순한 구조 유지

## DB 초기화 패턴

### Schema-First Initialization
- `schema.sql`: CREATE TABLE IF NOT EXISTS → 멱등성 보장
- `data.sql`: INSERT ... ON CONFLICT DO NOTHING → 중복 삽입 방지
- JPA ddl-auto=none → DDL은 SQL 파일에서만 관리
- 순서: schema.sql → Hibernate 검증 → data.sql

## 커넥션 관리 패턴

### HikariCP Connection Pool
- maximum-pool-size: 10 (기본값, 매장 규모에 충분)
- connection-timeout: 30s
- idle-timeout: 600s
- 별도 튜닝 불필요
