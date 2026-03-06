# 테이블오더 서비스 - User Stories

## 분류 방식: Persona-Based
## Acceptance Criteria 형식: Given/When/Then
## 우선순위: Must / Should / Could

---

# 고객 스토리 (Persona: 김민수)

## US-C01: 테이블 자동 로그인
**Priority**: Must

> 고객으로서, 테이블에 앉으면 별도 로그인 없이 바로 주문 화면을 보고 싶다.

**Acceptance Criteria:**

Scenario 1: 초기 설정 완료된 태블릿
  Given 관리자가 태블릿에 매장/테이블 정보를 설정했을 때
  When 태블릿 브라우저에서 서비스에 접속하면
  Then localStorage에 저장된 정보로 자동 로그인된다
  And 메뉴 화면이 바로 표시된다

Scenario 2: 초기 설정 미완료 태블릿
  Given 태블릿에 로그인 정보가 없을 때
  When 서비스에 접속하면
  Then 매장 식별자, 테이블 번호, 비밀번호 입력 화면이 표시된다

Scenario 3: 자동 로그인 실패
  Given 저장된 로그인 정보가 유효하지 않을 때
  When 자동 로그인을 시도하면
  Then 로그인 입력 화면으로 이동한다

---

## US-C02: 메뉴 카테고리별 조회
**Priority**: Must

> 고객으로서, 카테고리별로 메뉴를 탐색하여 원하는 메뉴를 빠르게 찾고 싶다.

**Acceptance Criteria:**

Scenario 1: 기본 메뉴 화면 진입
  Given 로그인이 완료되었을 때
  When 메뉴 화면에 진입하면
  Then 카테고리 목록과 첫 번째 카테고리의 메뉴가 카드 형태로 표시된다

Scenario 2: 카테고리 전환
  Given 메뉴 화면이 표시되어 있을 때
  When 다른 카테고리를 선택하면
  Then 해당 카테고리의 메뉴 목록으로 즉시 전환된다

Scenario 3: 메뉴 상세 정보 확인
  Given 메뉴 카드가 표시되어 있을 때
  When 메뉴 카드를 확인하면
  Then 메뉴명, 가격, 설명, 이미지가 표시된다
  And 맵기 수준이 설정된 메뉴는 맵기 수준이 함께 표시된다

---

## US-C03: 장바구니에 메뉴 추가
**Priority**: Must

> 고객으로서, 원하는 메뉴를 장바구니에 담아 주문 전에 모아두고 싶다.

**Acceptance Criteria:**

Scenario 1: 새 메뉴 추가
  Given 장바구니에 해당 메뉴가 없을 때
  When 메뉴 추가 버튼을 누르면
  Then 장바구니에 해당 메뉴가 수량 1로 추가된다
  And 총 금액이 갱신된다

Scenario 2: 기존 메뉴 수량 증가
  Given 장바구니에 해당 메뉴가 이미 있을 때
  When 같은 메뉴 추가 버튼을 누르면
  Then 해당 메뉴의 수량이 1 증가한다
  And 총 금액이 갱신된다

---

## US-C04: 장바구니 수정
**Priority**: Must

> 고객으로서, 장바구니에 담긴 메뉴의 수량을 조절하거나 삭제하고 싶다.

**Acceptance Criteria:**

Scenario 1: 수량 증가
  Given 장바구니에 메뉴가 있을 때
  When 수량 증가 버튼을 누르면
  Then 수량이 1 증가하고 총 금액이 갱신된다

Scenario 2: 수량 감소
  Given 장바구니에 수량 2 이상인 메뉴가 있을 때
  When 수량 감소 버튼을 누르면
  Then 수량이 1 감소하고 총 금액이 갱신된다

Scenario 3: 수량 1에서 감소 시 메뉴 제거
  Given 장바구니에 수량 1인 메뉴가 있을 때
  When 수량 감소 버튼을 누르면
  Then 수량이 0이 되므로 해당 메뉴가 장바구니에서 제거된다
  And 총 금액이 갱신된다

Scenario 4: 메뉴 삭제
  Given 장바구니에 메뉴가 있을 때
  When 삭제 버튼을 누르면
  Then 해당 메뉴가 장바구니에서 제거된다
  And 총 금액이 갱신된다

Scenario 5: 장바구니 비우기
  Given 장바구니에 메뉴가 있을 때
  When 장바구니 비우기를 실행하면
  Then 모든 메뉴가 제거되고 총 금액이 0원이 된다

Scenario 6: 새로고침 시 유지
  Given 장바구니에 메뉴가 담겨 있을 때
  When 페이지를 새로고침하면
  Then 장바구니 내용이 그대로 유지된다 (localStorage)

---

## US-C05: 주문 생성
**Priority**: Must

> 고객으로서, 장바구니의 메뉴를 확인하고 주문을 확정하고 싶다.

**Acceptance Criteria:**

Scenario 1: 주문 성공
  Given 장바구니에 메뉴가 있을 때
  When 주문 확정 버튼을 누르면
  Then 주문이 서버에 전송된다
  And 주문 번호가 표시된다
  And 장바구니가 비워진다
  And 5초 후 메뉴 화면으로 자동 리다이렉트된다

Scenario 2: 주문 실패
  Given 장바구니에 메뉴가 있을 때
  When 주문 확정 후 서버 오류가 발생하면
  Then 에러 메시지가 표시된다
  And 장바구니 내용이 유지된다

Scenario 3: 빈 장바구니 주문 방지
  Given 장바구니가 비어있을 때
  When 주문 화면에 진입하면
  Then 주문 확정 버튼이 비활성화된다

Scenario 4: 메뉴별 요청사항 입력
  Given 장바구니에 메뉴가 있을 때
  When 주문 확인 화면에서 메뉴별 요청사항 입력란에 텍스트를 입력하면 (예: "청양고추 빼주세요")
  Then 해당 요청사항이 주문 정보에 포함되어 서버로 전송된다

Scenario 5: 요청사항 미입력
  Given 장바구니에 메뉴가 있을 때
  When 요청사항을 입력하지 않고 주문을 확정하면
  Then 요청사항 없이 정상적으로 주문이 생성된다

Scenario 6: 맵기 옵션 선택
  Given 장바구니에 맵기 옵션이 설정된 메뉴가 있을 때
  When 주문 확인 화면에서 맵기 옵션을 선택하면
  Then 선택한 맵기 옵션이 주문 정보에 포함되어 서버로 전송된다

Scenario 7: 맵기 옵션 미설정 메뉴
  Given 장바구니에 맵기 옵션이 없는 메뉴가 있을 때
  When 주문 확인 화면을 보면
  Then 해당 메뉴에는 맵기 옵션 선택란이 표시되지 않는다

---

## US-C06: 주문 내역 조회
**Priority**: Must

> 고객으로서, 현재 테이블에서 주문한 내역을 확인하고 싶다.

**Acceptance Criteria:**

Scenario 1: 주문 내역 표시
  Given 현재 세션에서 주문이 있을 때
  When 주문 내역 화면에 진입하면
  Then 주문 시간 순으로 주문 목록이 표시된다
  And 각 주문에 주문 번호, 시각, 메뉴/수량, 금액, 상태가 표시된다

Scenario 2: 현재 세션 주문만 표시
  Given 이전 세션의 주문 이력이 있을 때
  When 주문 내역 화면에 진입하면
  Then 현재 테이블 세션의 주문만 표시된다
  And 이전 세션(이용 완료 처리된) 주문은 표시되지 않는다

Scenario 3: 주문 없음
  Given 현재 세션에서 주문이 없을 때
  When 주문 내역 화면에 진입하면
  Then "주문 내역이 없습니다" 메시지가 표시된다

---

## US-C07: 주문 상태 확인
**Priority**: Should

> 고객으로서, 주문한 메뉴의 준비 상태를 확인하고 싶다.

**Acceptance Criteria:**

Scenario 1: 주문 상태 표시
  Given 주문 내역이 있을 때
  When 주문 내역 화면을 보면
  Then 각 주문의 상태(대기중/준비중/완료)가 표시된다

Scenario 2: 상태 실시간 업데이트 (선택사항)
  Given 주문 내역 화면이 열려 있을 때
  When 관리자가 주문 상태를 변경하면
  Then 화면에 변경된 상태가 반영된다

---

## US-C08: 주문 내역 페이지네이션
**Priority**: Should

> 고객으로서, 주문이 많을 때 목록을 나눠서 보고 싶다.

**Acceptance Criteria:**

Scenario 1: 페이지네이션 또는 무한 스크롤
  Given 주문 내역이 한 페이지 분량을 초과할 때
  When 주문 내역 화면을 스크롤하면
  Then 추가 주문 내역이 로드된다

---

# 관리자 스토리 (Persona: 박서연)

## US-A01: 관리자 로그인
**Priority**: Must

> 관리자로서, 매장 관리 시스템에 안전하게 로그인하고 싶다.

**Acceptance Criteria:**

Scenario 1: 로그인 성공
  Given 유효한 매장 식별자, 사용자명, 비밀번호가 있을 때
  When 로그인 정보를 입력하고 로그인 버튼을 누르면
  Then JWT 토큰이 발급되고 관리자 대시보드로 이동한다
  And 세션이 16시간 유지된다

Scenario 2: 로그인 실패
  Given 잘못된 인증 정보를 입력했을 때
  When 로그인 버튼을 누르면
  Then 에러 메시지가 표시된다
  And 로그인 시도 횟수가 기록된다

Scenario 3: 세션 유지
  Given 로그인된 상태에서
  When 브라우저를 새로고침하면
  Then 세션이 유지되어 재로그인 없이 사용 가능하다

Scenario 4: 자동 로그아웃
  Given 로그인 후 16시간이 경과했을 때
  When 다음 요청을 보내면
  Then 자동으로 로그아웃되고 로그인 화면으로 이동한다

---

## US-A02: 실시간 주문 모니터링
**Priority**: Must

> 관리자로서, 들어오는 주문을 실시간으로 확인하고 싶다.

**Acceptance Criteria:**

Scenario 1: 대시보드 표시
  Given 관리자가 로그인했을 때
  When 주문 모니터링 화면에 진입하면
  Then 테이블별 카드가 그리드 레이아웃으로 표시된다
  And 각 카드에 총 주문액과 최신 주문 미리보기가 표시된다

Scenario 2: 신규 주문 실시간 수신
  Given 주문 모니터링 화면이 열려 있을 때
  When 고객이 새 주문을 생성하면
  Then 2초 이내에 해당 테이블 카드에 주문이 표시된다 (SSE)
  And 신규 주문이 시각적으로 강조된다 (색상 변경, 애니메이션)

Scenario 3: 주문 상세 보기
  Given 테이블 카드에 주문이 표시되어 있을 때
  When 주문 카드를 클릭하면
  Then 전체 메뉴 목록 상세 정보가 표시된다
  And 메뉴별 요청사항이 있는 경우 함께 표시된다
  And 메뉴별 선택된 맵기 옵션이 있는 경우 함께 표시된다

---

## US-A03: 주문 상태 변경
**Priority**: Must

> 관리자로서, 주문의 준비 상태를 변경하여 진행 상황을 관리하고 싶다.

**Acceptance Criteria:**

Scenario 1: 상태 변경
  Given 대기중 상태의 주문이 있을 때
  When 상태 변경 버튼을 누르면
  Then 주문 상태가 다음 단계로 변경된다 (대기중→준비중→완료)

Scenario 2: 주문 목록에서 축약 표시
  Given 주문 모니터링 화면에서
  When 테이블 카드의 주문 목록을 보면
  Then 주문 메뉴 및 수량이 축약 형태로 표시된다

---

## US-A04: 테이블별 필터링
**Priority**: Should

> 관리자로서, 특정 테이블의 주문만 필터링하여 보고 싶다.

**Acceptance Criteria:**

Scenario 1: 테이블 필터 적용
  Given 주문 모니터링 화면에서
  When 특정 테이블 번호로 필터링하면
  Then 해당 테이블의 주문만 표시된다

Scenario 2: 필터 해제
  Given 필터가 적용된 상태에서
  When 필터를 해제하면
  Then 전체 테이블의 주문이 다시 표시된다

---

## US-A05: 테이블 태블릿 초기 설정
**Priority**: Must

> 관리자로서, 테이블 태블릿의 초기 설정을 수행하여 고객이 바로 주문할 수 있게 하고 싶다.

**Acceptance Criteria:**

Scenario 1: 초기 설정 완료
  Given 관리자가 테이블 관리 화면에 있을 때
  When 테이블 번호와 비밀번호를 설정하면
  Then 16시간 세션이 생성된다
  And 해당 태블릿에서 자동 로그인이 활성화된다
  And 성공 피드백이 표시된다

Scenario 2: 설정 실패
  Given 잘못된 정보를 입력했을 때
  When 설정을 저장하면
  Then 실패 피드백이 표시된다

---

## US-A06: 주문 삭제
**Priority**: Must

> 관리자로서, 잘못된 주문을 삭제하여 정확한 주문 현황을 유지하고 싶다.

**Acceptance Criteria:**

Scenario 1: 주문 삭제 성공
  Given 테이블에 주문이 있을 때
  When 주문 삭제 버튼을 누르면
  Then 확인 팝업이 표시된다
  And 확인 시 주문이 즉시 삭제된다
  And 테이블 총 주문액이 재계산된다
  And 성공 피드백이 표시된다

Scenario 2: 삭제 취소
  Given 확인 팝업이 표시되었을 때
  When 취소를 선택하면
  Then 주문이 유지된다

Scenario 3: 삭제 실패
  Given 주문 삭제를 시도했을 때
  When 서버 오류가 발생하면
  Then 실패 피드백이 표시된다
  And 주문이 유지된다

---

## US-A07: 테이블 세션 시작
**Priority**: Must

> 관리자로서, 새 고객의 첫 주문 시 자동으로 테이블 세션이 시작되길 원한다.

**Acceptance Criteria:**

Scenario 1: 자동 세션 시작
  Given 테이블에 활성 세션이 없을 때
  When 해당 테이블에서 첫 주문이 생성되면
  Then 새로운 테이블 세션이 자동으로 시작된다
  And 세션 ID가 주문에 연결된다

---

## US-A08: 테이블 이용 완료 (세션 종료)
**Priority**: Must

> 관리자로서, 고객이 떠난 후 테이블을 초기화하여 다음 고객을 받고 싶다.

**Acceptance Criteria:**

Scenario 1: 이용 완료 처리
  Given 테이블에 활성 세션이 있을 때
  When 이용 완료 버튼을 누르면
  Then 확인 팝업이 표시된다
  And 확인 시 해당 세션의 주문 내역이 OrderHistory로 이동한다
  And 테이블 현재 주문 목록과 총 주문액이 0으로 리셋된다
  And 완료 시각이 기록된다
  And 성공 피드백이 표시된다

Scenario 2: 새 고객 시작
  Given 이용 완료 처리가 된 테이블에서
  When 새 고객이 주문을 시작하면
  Then 이전 주문 내역 없이 새로운 세션으로 시작된다

Scenario 3: 이용 완료 취소
  Given 확인 팝업이 표시되었을 때
  When 취소를 선택하면
  Then 세션이 유지된다

---

## US-A09: 과거 주문 내역 조회
**Priority**: Must

> 관리자로서, 테이블의 과거 주문 이력을 확인하고 싶다.

**Acceptance Criteria:**

Scenario 1: 과거 내역 조회
  Given 테이블 관리 화면에서
  When "과거 내역" 버튼을 누르면
  Then 해당 테이블의 과거 주문 목록이 시간 역순으로 표시된다
  And 각 주문에 주문 번호, 시각, 메뉴 목록, 총 금액, 매장 이용 완료 시각이 표시된다

Scenario 2: 날짜 필터링
  Given 과거 내역이 표시되어 있을 때
  When 날짜 필터를 적용하면
  Then 해당 날짜 범위의 주문만 표시된다

Scenario 3: 대시보드 복귀
  Given 과거 내역 화면이 열려 있을 때
  When "닫기" 버튼을 누르면
  Then 대시보드 화면으로 복귀한다

---

## US-A10: 메뉴 관리
**Priority**: Could

> 관리자로서, 매장 메뉴를 등록/수정/삭제하여 최신 상태로 유지하고 싶다.

**Acceptance Criteria:**

Scenario 1: 메뉴 등록
  Given 메뉴 관리 화면에서
  When 메뉴명, 가격, 설명, 카테고리, 이미지 URL을 입력하고 저장하면
  Then 새 메뉴가 등록된다
  And 필수 필드 미입력 시 검증 에러가 표시된다

Scenario 2: 메뉴 수정
  Given 등록된 메뉴가 있을 때
  When 메뉴 정보를 수정하고 저장하면
  Then 변경 사항이 반영된다

Scenario 3: 메뉴 삭제
  Given 등록된 메뉴가 있을 때
  When 삭제 버튼을 누르면
  Then 메뉴가 삭제된다

Scenario 4: 가격 범위 검증
  Given 메뉴 등록/수정 시
  When 유효하지 않은 가격을 입력하면
  Then 가격 범위 검증 에러가 표시된다

Scenario 5: 맵기 수준 설정
  Given 메뉴 등록/수정 화면에서
  When 맵기 수준을 선택하면 (안매움/약간매움/매움/아주매움)
  Then 고객 메뉴 화면에 해당 맵기 수준이 표시된다

Scenario 6: 맵기 옵션 설정
  Given 메뉴 등록/수정 화면에서
  When 고객이 선택 가능한 맵기 옵션 목록을 설정하면
  Then 고객 주문 시 해당 옵션 중 선택할 수 있다

Scenario 7: 맵기 미설정
  Given 메뉴 등록/수정 화면에서
  When 맵기 수준과 옵션을 설정하지 않으면
  Then 고객 화면에 맵기 관련 정보가 표시되지 않는다

---

## US-A11: 메뉴 노출 순서 조정
**Priority**: Could

> 관리자로서, 메뉴의 노출 순서를 조정하여 고객에게 원하는 순서로 보여주고 싶다.

**Acceptance Criteria:**

Scenario 1: 순서 변경
  Given 메뉴 관리 화면에서
  When 메뉴의 순서를 변경하면
  Then 고객 화면에서 변경된 순서로 메뉴가 표시된다

---

# 스토리 커버리지 매핑

| 요구사항 | 스토리 | 우선순위 |
|----------|--------|----------|
| FR-1.1 테이블 자동 로그인 | US-C01 | Must |
| FR-1.2 메뉴 조회 | US-C02 | Must |
| FR-1.3 장바구니 관리 | US-C03, US-C04 | Must |
| FR-1.4 주문 생성 | US-C05 (요청사항 입력 포함) | Must |
| FR-1.5 주문 내역 조회 | US-C06, US-C07, US-C08 | Must/Should |
| FR-2.1 매장 인증 | US-A01 | Must |
| FR-2.2 실시간 주문 모니터링 | US-A02, US-A03, US-A04 | Must/Should |
| FR-2.3 테이블 관리 | US-A05, US-A06, US-A07, US-A08, US-A09 | Must |
| FR-2.4 메뉴 관리 | US-A10, US-A11 | Could |
