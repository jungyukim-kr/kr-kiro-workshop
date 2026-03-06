# Unit 2: 인증 모듈 - Domain Entities

## Unit 2 관련 엔티티 범위

Unit 2는 Unit 1에서 정의된 엔티티(Store, Admin, StoreTable, TableSession)를 **참조**하며,
인증 로직에 필요한 추가 속성과 관계를 정의합니다.

> Unit 1의 domain-entities.md에 정의된 스키마가 기본이며, 이 문서는 인증 관점의 상세 설계입니다.

---

## 인증 관련 엔티티 상세

### Admin (관리자) - 인증 관점

**Unit 1 정의 참조**: `aidlc-docs/construction/unit-1-db-common/functional-design/domain-entities.md`

| 속성 | 인증 관점 용도 |
|------|---------------|
| username | 로그인 식별자 (store_id와 조합하여 고유) |
| password_hash | bcrypt 해싱된 비밀번호 (검증 대상) |

**인증 비즈니스 속성**:
- 비밀번호 변경 시 `password_hash` 업데이트
- 로그인 시도 제한 없음 (계정 잠금 기능 미적용)

### StoreTable (테이블) - 인증 관점

| 속성 | 인증 관점 용도 |
|------|---------------|
| table_number | 테이블 식별자 (store_id와 조합하여 고유) |
| password_hash | bcrypt 해싱된 4자리 PIN |

**인증 비즈니스 속성**:
- 테이블 인증은 로그인 시도 제한 없음 (물리적 접근 제어)
- 4자리 숫자 PIN만 허용

### TableSession (세션) - 인증 관점

| 속성 | 인증 관점 용도 |
|------|---------------|
| session_code | JWT 페이로드에 포함되는 세션 식별자 |
| active | 세션 유효성 검증에 사용 |

**인증 비즈니스 속성**:
- 세션은 첫 주문 시 자동 생성 (Unit 3/5에서 처리)
- JWT에 sessionId 포함 시, 활성 세션이 없으면 null 허용

---

## JWT Token (비영속 객체)

JWT 토큰은 DB에 저장하지 않는 비영속 객체입니다.

### JWT 페이로드 구조

| 클레임 | 타입 | 설명 | 관리자 | 테이블 |
|--------|------|------|--------|--------|
| sub | String | 주체 식별자 | admin:{adminId} | table:{tableId} |
| storeId | Long | 매장 ID | O | O |
| role | String | 역할 | ADMIN | TABLE |
| userId | Long | 사용자 ID | adminId | tableId |
| tableId | Long | 테이블 ID | null | tableId |
| sessionId | Long | 세션 ID | null | sessionId (nullable) |
| iat | Long | 발급 시각 | O | O |
| exp | Long | 만료 시각 | O | O |

### 토큰 타입별 차이

| 항목 | 관리자 토큰 | 테이블 토큰 |
|------|------------|------------|
| 만료 시간 | 16시간 | 16시간 |
| 갱신 정책 | 활동 감지 시 자동 연장 | 활동 감지 시 자동 연장 |
| 동시 로그인 | 단일 세션만 허용 | 제한 없음 |
| 저장 위치 | localStorage | localStorage |

---

## 엔티티 관계 (인증 관점)

```
+----------+     +-------+
|  Store   |1---N| Admin |
|          |     |       |
| store_code     | username
| password_hash  | password_hash
+----------+     +-------+
     |1
     |N
+----------+     +-----------+
|StoreTable|1---N|TableSession|
|          |     |           |
| table_number   | session_code
| password_hash  | active
+----------+     +-----------+
```

**관계 설명**:
- Store → Admin: 1:N (매장당 여러 관리자)
- Store → StoreTable: 1:N (매장당 여러 테이블)
- StoreTable → TableSession: 1:N (테이블당 여러 세션, 활성은 최대 1개)
