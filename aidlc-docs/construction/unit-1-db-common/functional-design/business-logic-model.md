# Unit 1: DB 스키마 + 공통 설정 - Business Logic Model

## 공통 설정 범위

### Docker Compose 구성
- PostgreSQL 컨테이너 (포트 5432)
- Spring Boot 앱 컨테이너 (포트 8080)
- 프론트엔드 정적 파일 서빙 (Spring Boot에서 처리 또는 별도 nginx)

### Spring Boot 설정
- JPA/Hibernate + PostgreSQL 연결
- JWT 설정 (secret, 만료시간 16h)
- CORS 설정 (프론트엔드 허용)
- 정적 리소스 서빙 (frontend/ 디렉토리)

### DB 초기화
- schema.sql: 테이블 생성 DDL
- data.sql: 시드 데이터 (매장, 관리자, 샘플 메뉴)
- spring.sql.init.mode=always (개발 환경)

### 주문 번호 생성 로직
- 형식: `#{매장별 일련번호}` (예: #A-0042)
- 매장별 독립 채번
- DB 시퀀스 또는 당일 최대값+1 방식
