# Unit 1: DB 스키마 + 공통 설정 - Infrastructure Design

## 배포 환경: Local Docker Compose

### 컨테이너 구성

| 서비스 | 이미지 | 포트 | 역할 |
|--------|--------|------|------|
| app | 빌드 (Dockerfile) | 8080:8080 | Spring Boot 앱 |
| db | postgres:16-alpine | 5432:5432 | PostgreSQL DB |

### PostgreSQL 설정
- 이미지: `postgres:16-alpine` (경량)
- 볼륨: `pgdata:/var/lib/postgresql/data` (데이터 영속화)
- 환경변수: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- healthcheck: `pg_isready` 사용

### Spring Boot 앱 설정
- Dockerfile: multi-stage build (Gradle build → JRE 17 실행)
- `app` 서비스는 `db` 서비스에 `depends_on` (healthcheck 조건)
- 환경변수로 DB 접속 정보 주입
- 프론트엔드 정적 파일은 앱 내 `/frontend/` 경로로 서빙

### 네트워크
- Docker Compose 기본 네트워크 사용
- 앱 → DB 접속: `db:5432` (서비스명으로 접근)

### 볼륨
- `pgdata`: PostgreSQL 데이터 영속화 (named volume)
