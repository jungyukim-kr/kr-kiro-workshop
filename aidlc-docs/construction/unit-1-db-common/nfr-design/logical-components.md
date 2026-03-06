# Unit 1: DB 스키마 + 공통 설정 - Logical Components

## 인프라 컴포넌트

```
┌─────────────────────────────────────────┐
│           Docker Compose                │
│                                         │
│  ┌──────────────┐  ┌────────────────┐   │
│  │ Spring Boot  │──│  PostgreSQL    │   │
│  │   :8080      │  │    :5432       │   │
│  │              │  │                │   │
│  │ - JPA/Hibernate│ │ - Volume 영속화│   │
│  │ - HikariCP   │  │ - schema.sql  │   │
│  │ - JWT Filter │  │ - data.sql    │   │
│  └──────────────┘  └────────────────┘   │
└─────────────────────────────────────────┘
```

## Spring Boot 내부 구조

| 컴포넌트 | 역할 | 설정 |
|----------|------|------|
| DataSource (HikariCP) | DB 커넥션 풀 | application.yml |
| JPA/Hibernate | ORM, 엔티티 매핑 | ddl-auto=none |
| Spring Security FilterChain | JWT 인증 필터 | SecurityConfig |
| Jackson ObjectMapper | JSON 직렬화 | 기본 설정 |
| Static Resource Handler | 프론트엔드 서빙 | /frontend/** |

## 외부 의존성 없음
- 메시지 큐: 불필요 (SSE로 실시간 통신)
- 캐시: 불필요 (매장 규모 트래픽)
- 외부 API: 없음
- 파일 스토리지: 없음 (이미지는 외부 URL)
