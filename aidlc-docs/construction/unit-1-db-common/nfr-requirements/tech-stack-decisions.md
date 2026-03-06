# Unit 1: DB 스키마 + 공통 설정 - Tech Stack Decisions

## 확정 기술 스택

| 영역 | 기술 | 버전 | 선택 근거 |
|------|------|------|-----------|
| Language | Java | 17+ | 요구사항 확정 |
| Framework | Spring Boot | 3.x | 요구사항 확정 |
| ORM | Spring Data JPA + Hibernate | - | Spring Boot 표준 |
| DB | PostgreSQL | 16 | 요구사항 확정 |
| 커넥션 풀 | HikariCP | - | Spring Boot 기본 내장 |
| 빌드 | Gradle (Groovy) | 8.x | Java 프로젝트 표준 |
| 컨테이너 | Docker Compose | - | 요구사항 확정 (로컬 배포) |
| 인증 | JWT (HS256) | - | 요구사항 확정 |
| 해싱 | bcrypt (Spring Security) | - | 비밀번호 해싱 표준 |

## DB 초기화 전략
- `spring.sql.init.mode=always`
- `schema.sql` → DDL (CREATE TABLE IF NOT EXISTS)
- `data.sql` → 시드 데이터 (INSERT ... ON CONFLICT DO NOTHING)
- JPA `ddl-auto=none` (DDL은 schema.sql에서만 관리)

## 프로젝트 구조
- 단일 Spring Boot 앱 (모놀리스)
- 패키지 분리: `com.tableorder.common`, `com.tableorder.customer`, `com.tableorder.admin`
- 프론트엔드: Spring Boot 정적 리소스 서빙 (`/frontend/`)
