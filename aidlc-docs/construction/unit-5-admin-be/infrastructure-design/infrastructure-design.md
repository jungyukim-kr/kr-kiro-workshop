# Unit 5: Admin Backend - Infrastructure Design

## 인프라 변경사항
없음 — Unit 1 기존 Docker Compose 인프라 그대로 활용.

- app: Spring Boot 8080 (Unit 5 코드 추가)
- db: PostgreSQL 16 5432 (기존 스키마 사용)
- SSE는 Spring 내장 SseEmitter 사용 (별도 인프라 불필요)
