# Business Rules - Unit 4: 고객 프론트엔드

## 1. 인증 규칙 (Authentication Rules)

### BR-AUTH-01: 자동 로그인
- localStorage에 `auth_storeCode`, `auth_tableNumber`, `auth_password`, `auth_token`이 모두 존재하면 자동 로그인 시도
- `GET /api/auth/validate`로 토큰 유효성 확인
- 유효하면 메뉴 화면으로 이동
- 무효하면 저장된 credentials로 `POST /api/auth/table/login` 재시도
- 재시도도 실패하면 localStorage 인증 정보 삭제 후 로그인 화면 표시

### BR-AUTH-02: 로그인 입력 검증
- 매장 식별자: 필수, 빈 문자열 불가
- 테이블 번호: 필수, 양의 정수
- PIN: 필수, 정확히 4자리 숫자 (`/^\d{4}$/`)

### BR-AUTH-03: 토큰 자동 갱신
- 모든 API 응답에서 `X-New-Token` 헤더 확인
- 헤더가 존재하면 localStorage의 `auth_token` 즉시 교체
- 이후 API 요청은 새 토큰 사용

### BR-AUTH-04: 인증 실패 처리
- 401 응답 수신 시: 자동 로그인 재시도 (BR-AUTH-01 절차)
- 재시도 실패 시: 로그인 화면으로 이동

---

## 2. 장바구니 규칙 (Cart Rules)

### BR-CART-01: 메뉴 추가
- 메뉴 카드의 + 버튼 클릭 시 장바구니에 해당 메뉴 추가
- 이미 존재하는 메뉴면 수량 +1
- 새 메뉴면 수량 1로 CartItem 생성
- 즉시 localStorage에 반영

### BR-CART-02: 수량 조절
- + 버튼: 수량 +1
- - 버튼: 수량 -1
- 수량이 0이 되면 해당 항목 자동 제거
- 최소 수량: 0 (제거), 최대 수량 제한 없음

### BR-CART-03: 항목 삭제
- 개별 삭제(X 버튼): 해당 CartItem 제거
- 전체 비우기: 모든 CartItem 제거
- 삭제 후 즉시 localStorage 반영

### BR-CART-04: 총 금액 계산
- `totalAmount = Σ(cartItem.unitPrice × cartItem.quantity)`
- 장바구니 변경 시마다 재계산
- 사이드 패널 하단 + 장바구니 플로팅 버튼에 표시

### BR-CART-05: 장바구니 영속성
- 모든 장바구니 변경은 즉시 localStorage에 저장
- 페이지 새로고침 시 localStorage에서 복원
- 주문 완료 시에만 장바구니 비우기

### BR-CART-06: 메뉴 카드 수량 동기화
- 메뉴 화면의 각 카드에 장바구니 수량 표시
- 장바구니에 없는 메뉴: + 버튼만 표시
- 장바구니에 있는 메뉴: -[수량]+ 형태로 표시

---

## 3. 주문 규칙 (Order Rules)

### BR-ORDER-01: 주문 가능 조건
- 장바구니에 1개 이상의 항목이 있어야 주문 가능
- 빈 장바구니: "주문 확정" 버튼 비활성화

### BR-ORDER-02: 맵기 옵션 처리
- `hasSpicyOptions=true`인 메뉴만 맵기 옵션 드롭다운 표시
- 주문 확인 화면 진입 시 해당 메뉴의 spicy-options API 호출
- 옵션 선택은 선택사항 (미선택 시 null 전송)
- `hasSpicyOptions=false`인 메뉴: 맵기 옵션 UI 미표시, null 전송

### BR-ORDER-03: 요청사항 처리
- 모든 메뉴 항목에 요청사항 입력란 표시
- 입력은 선택사항 (미입력 시 null 전송)
- 텍스트 자유 입력

### BR-ORDER-04: 주문 전송 데이터 구성
- CartItem[] → OrderRequest 변환
- 각 항목: menuId, quantity, unitPrice, spicyOption, specialRequest
- unitPrice는 장바구니 추가 시점의 메뉴 가격

### BR-ORDER-05: 주문 성공 후 처리
1. 주문 완료 토스트 알림 (주문 번호 표시)
2. 장바구니 비우기 (localStorage 포함)
3. 응답의 sessionId로 localStorage의 `auth_sessionId` 업데이트
4. 5초 후 메뉴 화면으로 자동 이동

### BR-ORDER-06: 주문 실패 처리
- 에러 메시지 토스트 표시
- 장바구니 내용 유지 (삭제하지 않음)
- PRICE_MISMATCH(409): "메뉴 가격이 변경되었습니다. 장바구니를 확인해주세요" 알림

### BR-ORDER-07: 중복 주문 방지
- "주문 확정" 버튼 클릭 후 API 응답까지 버튼 비활성화 + 로딩 표시
- 응답 수신 후 버튼 상태 복원

---

## 4. 주문 내역 규칙 (Order History Rules)

### BR-HISTORY-01: 주문 내역 조회
- 현재 세션의 주문만 표시 (서버에서 세션 기반 필터링)
- 최신순 정렬 (createdAt 내림차순)
- sessionId가 null이면 빈 목록 표시

### BR-HISTORY-02: 주문 상태 표시
- WAITING: 노란색 뱃지 "대기중"
- PREPARING: 파란색 뱃지 "준비중"
- DONE: 초록색 뱃지 "완료"

### BR-HISTORY-03: 무한 스크롤 페이지네이션
- 초기 로드: page=0, size=10
- 스크롤 하단 도달 시 다음 페이지 로드
- `page >= totalPages - 1`이면 추가 로드 중단
- 로딩 중 중복 요청 방지

---

## 5. 입력 검증 규칙 (Validation Rules)

### BR-VAL-01: 로그인 폼
| 필드 | 규칙 | 에러 메시지 |
|------|------|------------|
| 매장 식별자 | 필수 | "매장 식별자를 입력해주세요" |
| 테이블 번호 | 필수, 양의 정수 | "테이블 번호를 입력해주세요" |
| PIN | 필수, 4자리 숫자 | "4자리 숫자 PIN을 입력해주세요" |

### BR-VAL-02: 장바구니 수량
| 규칙 | 처리 |
|------|------|
| 수량 < 0 | 불가 (0이면 제거) |
| 수량 = 0 | 항목 자동 제거 |

---

## 6. 에러 처리 규칙 (Error Handling Rules)

### BR-ERR-01: API 에러 매핑

| HTTP Status | Error Code | 사용자 메시지 | 동작 |
|-------------|------------|--------------|------|
| 400 | VALIDATION_FAILED | 서버 메시지 표시 | 토스트 알림 |
| 400 | INVALID_SPICY_OPTION | 서버 메시지 표시 | 토스트 알림 |
| 401 | TOKEN_EXPIRED | "세션이 만료되었습니다" | 자동 재로그인 시도 |
| 401 | TOKEN_INVALID | "인증 정보가 유효하지 않습니다" | 로그인 화면 이동 |
| 404 | MENU_NOT_FOUND | "메뉴를 찾을 수 없습니다" | 토스트 알림 |
| 409 | PRICE_MISMATCH | "메뉴 가격이 변경되었습니다" | 토스트 알림 + 장바구니 유지 |
| 500 | INTERNAL_ERROR | "서버 오류가 발생했습니다" | 토스트 알림 |
| 네트워크 오류 | - | "네트워크 연결을 확인해주세요" | 토스트 알림 |

### BR-ERR-02: 네트워크 오류 처리
- fetch 실패 (네트워크 단절): "네트워크 연결을 확인해주세요" 토스트
- 타임아웃 (10초): "요청 시간이 초과되었습니다" 토스트
