# Functional Design Plan - Unit 4: 고객 프론트엔드

## Unit Context
- **Unit**: unit-4-customer-fe
- **팀**: 고객팀
- **기술 스택**: Vanilla JS + HTML + CSS (SPA 없음, 정적 파일)
- **Stories**: US-C01~C08 (8개)
- **의존**: Unit 2 (Auth API), Unit 3 (Customer API)

## Plan Steps

- [x] 1. Unit 3 API 규격서 + Unit 2 Auth API 규격서 분석
- [x] 2. 사용자 질문 수집 및 답변 분석
- [x] 3. Frontend Components 설계 (화면 구조, 컴포넌트 계층)
- [x] 4. Domain Entities 설계 (클라이언트 데이터 모델, localStorage 구조)
- [x] 5. Business Rules 설계 (장바구니 로직, 입력 검증, 상태 관리)
- [x] 6. Business Logic Model 설계 (화면 흐름, API 연동, 에러 처리)

---

## 질문

### 화면 구조 및 네비게이션

**Q1**: 고객 화면의 전체 레이아웃 구조를 어떻게 구성할까요?

A) SPA 스타일 (단일 HTML, JavaScript로 화면 전환)
B) MPA 스타일 (화면별 별도 HTML 파일)
C) 하이브리드 (메인 HTML 1개 + 모달/패널로 화면 전환)

[Answer]: A (AI 추천 - SPA 스타일: Vanilla JS 단일 HTML에서 JS로 화면 전환, 테이블 오더 특성상 페이지 리로드 없는 UX 적합)

**Q2**: 하단 네비게이션 바를 사용할까요?

A) 하단 고정 네비게이션 (메뉴/장바구니/주문내역 탭)
B) 상단 헤더에 네비게이션 포함
C) 네비게이션 없이 화면 내 버튼으로 이동

[Answer]:B

### 장바구니 UI

**Q3**: 장바구니 표시 방식은?

A) 하단 플로팅 바 (총 금액 + 수량 표시, 클릭 시 장바구니 화면 이동)
B) 사이드 패널 (슬라이드 인/아웃)
C) 별도 전체 화면

[Answer]:B

**Q4**: 메뉴 카드에서 장바구니 추가 시 인터랙션은?

A) 바로 수량 1 추가 (토스트 알림)
B) 수량 선택 모달 후 추가
C) 메뉴 카드에 +/- 버튼 직접 표시

[Answer]:C

### 주문 확인 화면

**Q5**: 주문 확인 화면에서 맵기 옵션과 요청사항 입력 위치는?

A) 주문 확인 화면에서 각 메뉴 항목 아래에 인라인 표시
B) 각 메뉴 항목 클릭 시 모달로 입력
C) 장바구니 화면에서 미리 입력

[Answer]:A

### 디자인 및 스타일

**Q6**: 디자인 프레임워크 사용 여부는?

A) 순수 CSS (커스텀 디자인)
B) Bootstrap 5 CDN
C) Tailwind CSS CDN

[Answer]: B (AI 추천 - Bootstrap 5 CDN: 반응형 그리드 + 컴포넌트 활용, Vanilla JS와 호환성 좋음, 빠른 개발)

**Q7**: 태블릿 화면 크기 기준은?

A) 10인치 태블릿 최적화 (1024x768 기준)
B) 반응형 (모바일~태블릿)
C) 고정 너비 (태블릿 전용)

[Answer]:B

### 상태 관리 및 토큰

**Q8**: JWT 토큰 자동 갱신(X-New-Token) 처리를 어떻게 할까요?

A) 모든 API 응답에서 X-New-Token 헤더 확인 후 자동 교체 (공통 fetch wrapper)
B) 별도 인터셉터 없이 필요 시 수동 처리

[Answer]:A
