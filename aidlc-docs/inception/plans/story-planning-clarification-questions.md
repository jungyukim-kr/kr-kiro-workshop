# Story Planning - Clarification Questions

Question 2에서 Acceptance Criteria 형식에 대해 질문을 주셨습니다.
아래 예시를 비교한 후 선택해주세요.

---

## 두 형식의 차이 (예시: "고객이 장바구니에 메뉴를 추가한다")

### A) 간결 형식
```
Acceptance Criteria:
- 메뉴 카드에서 추가 버튼을 누르면 장바구니에 담긴다
- 이미 담긴 메뉴를 다시 추가하면 수량이 1 증가한다
- 총 금액이 실시간으로 갱신된다
```

### B) Given/When/Then 형식
```
Acceptance Criteria:

Scenario 1: 새 메뉴 추가
  Given 장바구니가 비어있을 때
  When 고객이 "김치찌개" 추가 버튼을 누르면
  Then 장바구니에 "김치찌개" x1이 추가된다
  And 총 금액이 8,000원으로 표시된다

Scenario 2: 기존 메뉴 수량 증가
  Given 장바구니에 "김치찌개" x1이 있을 때
  When 고객이 "김치찌개" 추가 버튼을 다시 누르면
  Then 수량이 2로 변경된다
  And 총 금액이 16,000원으로 갱신된다
```

### 비교 요약

| 항목 | A) 간결 | B) Given/When/Then |
|------|---------|-------------------|
| 분량 | 짧음 (3-5줄) | 길음 (시나리오당 4-5줄) |
| 명확성 | 핵심만 전달 | 구체적 시나리오 포함 |
| 테스트 연계 | 별도 해석 필요 | 바로 테스트 케이스로 활용 가능 |
| 적합한 경우 | 빠른 개발, 단순 기능 | 복잡한 비즈니스 로직, 품질 중시 |

---

## Clarification Question 1
위 예시를 참고하여 Acceptance Criteria 형식을 선택해주세요.

A) 간결 - 핵심 조건만 3-5개 항목으로 기술
B) 상세 - Given/When/Then 형식으로 시나리오별 기술
C) Other (please describe after [Answer]: tag below)

[Answer]: B
