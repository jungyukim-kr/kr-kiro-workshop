# Unit 1: DB 스키마 + 공통 설정 - Deployment Architecture

```
┌─────────────────────────────────────────────┐
│              Docker Compose                 │
│                                             │
│  ┌─────────────────┐  ┌──────────────────┐  │
│  │   app (:8080)   │  │  db (:5432)      │  │
│  │                 │  │                  │  │
│  │  Spring Boot 3  │──│  PostgreSQL 16   │  │
│  │  JRE 17         │  │  alpine          │  │
│  │                 │  │                  │  │
│  │  /api/**   REST │  │  Volume: pgdata  │  │
│  │  /frontend/** 정적│  │                  │  │
│  └─────────────────┘  └──────────────────┘  │
│                                             │
└─────────────────────────────────────────────┘
        │                       │
     localhost:8080          localhost:5432
```

## 실행 방법
```bash
# 전체 시작
docker compose up --build

# 백그라운드 실행
docker compose up --build -d

# 종료
docker compose down

# DB 데이터 포함 초기화
docker compose down -v
```

## 접속 URL
- 고객 화면: http://localhost:8080/frontend/customer/
- 관리자 화면: http://localhost:8080/frontend/admin/
- API: http://localhost:8080/api/**
- DB 직접 접속: localhost:5432
