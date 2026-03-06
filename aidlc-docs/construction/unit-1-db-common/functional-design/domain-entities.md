# Unit 1: DB 스키마 + 공통 설정 - Domain Entities

## ER 다이어그램

```
+----------+     +-------+     +----------+     +-----------+
|  Store   |1---N| Admin |     |  Table   |1---N|  Session  |
+----------+     +-------+     +----------+     +-----------+
     |1                             |1               |1
     |N                             |                |N
+----------+                        |           +----------+
|   Menu   |                        |           |  Order   |
+----------+                        |           +----------+
     |1                             |                |1
     |N                             |                |N
+---------------+                   |           +----------+
| MenuSpicyOption|                  |           | OrderItem|
+---------------+                   |           +----------+
                                    |
                              +----------+
                              |OrderHistory|
                              +----------+
                                    |1
                                    |N
                              +-----------+
                              |OrderHistoryItem|
                              +-----------+
```

## 엔티티 정의

### Store (매장)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 매장 ID |
| store_code | VARCHAR(50) | UNIQUE, NOT NULL | 매장 식별자 |
| name | VARCHAR(100) | NOT NULL | 매장명 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |

### Admin (관리자)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 관리자 ID |
| store_id | BIGINT | FK(Store), NOT NULL | 매장 ID |
| username | VARCHAR(50) | NOT NULL | 사용자명 |
| password_hash | VARCHAR(255) | NOT NULL | bcrypt 해시 |
| login_attempts | INT | DEFAULT 0 | 로그인 시도 횟수 |
| locked_until | TIMESTAMP | NULLABLE | 잠금 해제 시각 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| | | UNIQUE(store_id, username) | |

### StoreTable (테이블)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 테이블 ID |
| store_id | BIGINT | FK(Store), NOT NULL | 매장 ID |
| table_number | INT | NOT NULL | 테이블 번호 |
| password_hash | VARCHAR(255) | NOT NULL | bcrypt 해시 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| | | UNIQUE(store_id, table_number) | |

### TableSession (테이블 세션)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 세션 ID |
| store_id | BIGINT | FK(Store), NOT NULL | 매장 ID |
| table_id | BIGINT | FK(StoreTable), NOT NULL | 테이블 ID |
| session_code | VARCHAR(50) | UNIQUE, NOT NULL | 세션 식별 코드 |
| started_at | TIMESTAMP | NOT NULL | 세션 시작 시각 |
| ended_at | TIMESTAMP | NULLABLE | 세션 종료 시각 (이용 완료) |
| active | BOOLEAN | DEFAULT true | 활성 여부 |

### Menu (메뉴)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 메뉴 ID |
| store_id | BIGINT | FK(Store), NOT NULL | 매장 ID |
| name | VARCHAR(100) | NOT NULL | 메뉴명 |
| price | INT | NOT NULL, CHECK(price >= 0) | 가격 (원) |
| description | TEXT | NULLABLE | 설명 |
| category | VARCHAR(50) | NOT NULL | 카테고리 |
| image_url | VARCHAR(500) | NULLABLE | 이미지 URL |
| spicy_level | VARCHAR(20) | NULLABLE | 맵기 수준 (안매움/약간매움/매움/아주매움) |
| display_order | INT | DEFAULT 0 | 노출 순서 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |

### MenuSpicyOption (메뉴 맵기 옵션)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 옵션 ID |
| menu_id | BIGINT | FK(Menu), NOT NULL | 메뉴 ID |
| option_name | VARCHAR(50) | NOT NULL | 옵션명 (순한맛/보통/매운맛/아주매운맛) |
| display_order | INT | DEFAULT 0 | 표시 순서 |

### Orders (주문)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 주문 ID |
| store_id | BIGINT | FK(Store), NOT NULL | 매장 ID |
| table_id | BIGINT | FK(StoreTable), NOT NULL | 테이블 ID |
| session_id | BIGINT | FK(TableSession), NOT NULL | 세션 ID |
| order_number | VARCHAR(20) | UNIQUE, NOT NULL | 주문 번호 (표시용) |
| total_amount | INT | NOT NULL | 총 금액 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'WAITING' | 상태 (WAITING/PREPARING/DONE) |
| created_at | TIMESTAMP | NOT NULL | 주문 시각 |

### OrderItem (주문 항목)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 항목 ID |
| order_id | BIGINT | FK(Orders), NOT NULL | 주문 ID |
| menu_id | BIGINT | FK(Menu), NOT NULL | 메뉴 ID |
| menu_name | VARCHAR(100) | NOT NULL | 메뉴명 (스냅샷) |
| quantity | INT | NOT NULL, CHECK(quantity > 0) | 수량 |
| unit_price | INT | NOT NULL | 단가 (스냅샷) |
| spicy_option | VARCHAR(50) | NULLABLE | 선택한 맵기 옵션 |
| special_request | TEXT | NULLABLE | 요청사항 |

### OrderHistory (과거 주문 이력)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 이력 ID |
| store_id | BIGINT | FK(Store), NOT NULL | 매장 ID |
| table_id | BIGINT | FK(StoreTable), NOT NULL | 테이블 ID |
| session_code | VARCHAR(50) | NOT NULL | 세션 코드 |
| order_number | VARCHAR(20) | NOT NULL | 주문 번호 |
| total_amount | INT | NOT NULL | 총 금액 |
| ordered_at | TIMESTAMP | NOT NULL | 주문 시각 |
| completed_at | TIMESTAMP | NOT NULL | 이용 완료 시각 |

### OrderHistoryItem (과거 주문 항목)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGSERIAL | PK | 항목 ID |
| order_history_id | BIGINT | FK(OrderHistory), NOT NULL | 이력 ID |
| menu_name | VARCHAR(100) | NOT NULL | 메뉴명 |
| quantity | INT | NOT NULL | 수량 |
| unit_price | INT | NOT NULL | 단가 |
| spicy_option | VARCHAR(50) | NULLABLE | 맵기 옵션 |
| special_request | TEXT | NULLABLE | 요청사항 |
