# Unit 1: DB 스키마 + 공통 설정 - Business Rules

## 데이터 무결성 규칙

### Store
- store_code는 매장 간 고유해야 함

### Admin
- (store_id, username) 조합은 고유해야 함
- password_hash는 bcrypt로 해싱

### StoreTable
- (store_id, table_number) 조합은 고유해야 함
- password_hash는 bcrypt로 해싱

### Menu
- price >= 0
- name, category는 필수
- spicy_level 허용값: NULL, '안매움', '약간매움', '매움', '아주매움'
- display_order로 카테고리 내 정렬

### Orders
- status 허용값: 'WAITING', 'PREPARING', 'DONE'
- 상태 전이: WAITING → PREPARING → DONE (역방향 불가)
- order_number 형식: 매장코드-날짜-순번 (예: A-0042)

### OrderItem
- quantity > 0
- menu_name, unit_price는 주문 시점 스냅샷 (메뉴 변경 시에도 주문 기록 유지)

### TableSession
- 테이블당 활성 세션은 최대 1개 (active=true)
- 세션 종료 시 active=false, ended_at 기록

## 시드 데이터 규칙

### 관리자 계정 (사전 설정)
- 매장별 1개 이상의 관리자 계정 시드
- 비밀번호는 bcrypt 해싱된 상태로 저장

### 샘플 매장/메뉴
- 데모용 매장 1개 + 카테고리별 샘플 메뉴 포함
