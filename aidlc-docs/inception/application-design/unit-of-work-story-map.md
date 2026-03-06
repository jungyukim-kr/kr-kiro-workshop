# Unit of Work - Story Map

## 스토리 → Unit 매핑

| Story | 설명 | Unit (BE) | Unit (FE) | 팀 | Priority |
|-------|------|-----------|-----------|-----|----------|
| US-C01 | 테이블 자동 로그인 | Unit 2 (인증) | Unit 4 (고객 FE) | 공통+고객 | Must |
| US-C02 | 메뉴 카테고리별 조회 | Unit 3 (고객 BE) | Unit 4 (고객 FE) | 고객 | Must |
| US-C03 | 장바구니에 메뉴 추가 | - | Unit 4 (고객 FE) | 고객 | Must |
| US-C04 | 장바구니 수정 | - | Unit 4 (고객 FE) | 고객 | Must |
| US-C05 | 주문 생성 | Unit 3 (고객 BE) | Unit 4 (고객 FE) | 고객 | Must |
| US-C06 | 주문 내역 조회 | Unit 3 (고객 BE) | Unit 4 (고객 FE) | 고객 | Must |
| US-C07 | 주문 상태 확인 | Unit 3 (고객 BE) | Unit 4 (고객 FE) | 고객 | Should |
| US-C08 | 주문 내역 페이지네이션 | Unit 3 (고객 BE) | Unit 4 (고객 FE) | 고객 | Should |
| US-A01 | 관리자 로그인 | Unit 2 (인증) | Unit 6 (관리자 FE) | 공통+관리자 | Must |
| US-A02 | 실시간 주문 모니터링 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Must |
| US-A03 | 주문 상태 변경 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Must |
| US-A04 | 테이블별 필터링 | - | Unit 6 (관리자 FE) | 관리자 | Should |
| US-A05 | 테이블 태블릿 초기 설정 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Must |
| US-A06 | 주문 삭제 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Must |
| US-A07 | 테이블 세션 시작 | Unit 5 (관리자 BE) | - | 관리자 | Must |
| US-A08 | 테이블 이용 완료 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Must |
| US-A09 | 과거 주문 내역 조회 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Must |
| US-A10 | 메뉴 관리 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Could |
| US-A11 | 메뉴 노출 순서 조정 | Unit 5 (관리자 BE) | Unit 6 (관리자 FE) | 관리자 | Could |

## 팀별 스토리 수

| 팀 | Unit | Must | Should | Could | 합계 |
|----|------|------|--------|-------|------|
| 공통 | Unit 1 (DB/설정) | - | - | - | 기반 |
| 공통 | Unit 2 (인증) | 2 | 0 | 0 | 2 |
| 고객팀 | Unit 3 (고객 BE) | 4 | 2 | 0 | 6 |
| 고객팀 | Unit 4 (고객 FE) | 6 | 2 | 0 | 8 |
| 관리자팀 | Unit 5 (관리자 BE) | 7 | 0 | 2 | 9 |
| 관리자팀 | Unit 6 (관리자 FE) | 7 | 1 | 2 | 10 |

> 모든 19개 스토리가 unit에 매핑되었습니다. 누락 없음.
