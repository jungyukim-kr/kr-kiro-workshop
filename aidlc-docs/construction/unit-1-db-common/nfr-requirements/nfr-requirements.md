# Unit 1: DB 스키마 + 공통 설정 - NFR Requirements

## 성능 (Performance)
- DB 쿼리 응답: 단일 테이블 조회 < 100ms
- 동시 접속: 매장당 테이블 수 기준 (수십 개 수준), 고부하 고려 불필요
- 커넥션 풀: HikariCP 기본 설정 (max 10) 충분

## 가용성 (Availability)
- 로컬/Docker Compose 배포 — HA 구성 불필요
- PostgreSQL 단일 인스턴스
- 앱 재시작 시 DB 데이터 유지 (Docker volume)

## 보안 (Security)
- 비밀번호: bcrypt 해싱 (cost factor 10)
- JWT: HS256, 만료 16시간
- SQL Injection 방지: JPA Parameterized Query 사용
- DB 접속 정보: application.yml 환경변수 또는 Docker Compose 환경변수

## 데이터 무결성 (Data Integrity)
- FK 제약조건 사용 — 고아 레코드 원천 차단
- NOT NULL, UNIQUE, CHECK 제약조건 적용
- 주문 상태 전이 규칙은 애플리케이션 레벨에서 강제

## 유지보수성 (Maintainability)
- DDL: schema.sql로 관리 (JPA auto-ddl 사용 안 함)
- 시드 데이터: data.sql로 관리
- 엔티티 클래스와 DB 스키마 1:1 매핑

## 확장성 (Scalability)
- MVP 단계에서 수평 확장 고려 불필요
- 단일 매장 → 다중 매장은 store_id 기반 데이터 분리로 이미 설계됨
