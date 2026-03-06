# Unit of Work - 의존성

## 의존성 매트릭스

| Unit | 팀 | 의존 대상 | 의존 유형 |
|------|----|-----------|-----------|
| Unit 1: DB/설정 | 공통 | (없음) | - |
| Unit 2: 인증 | 공통 | Unit 1 | DB 스키마, Spring 설정 |
| Unit 3: 고객 백엔드 | 고객팀 | Unit 1, Unit 2 | DB, 인증, 공통 엔티티 |
| Unit 4: 고객 프론트엔드 | 고객팀 | Unit 3 | REST API 호출 |
| Unit 5: 관리자 백엔드 | 관리자팀 | Unit 1, Unit 2 | DB, 인증, 공통 엔티티 |
| Unit 6: 관리자 프론트엔드 | 관리자팀 | Unit 5 | REST API + SSE 호출 |

## 구현 순서 및 병렬화

```
Unit 1 (DB/설정) ─── 공통
  │
  └── Unit 2 (인증) ─── 공통
        │
        ├── Unit 3 (고객 BE) ──── 고객팀 ──→ Unit 4 (고객 FE)
        │
        └── Unit 5 (관리자 BE) ── 관리자팀 ─→ Unit 6 (관리자 FE)
```

**병렬 구현 가능**: Unit 1,2 완료 후 고객팀(Unit 3→4)과 관리자팀(Unit 5→6)이 동시 진행 가능
