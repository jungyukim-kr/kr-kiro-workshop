# Unit 1: Code Generation Summary

## 생성된 파일

### 빌드/설정
- `backend/build.gradle` — Spring Boot 3.4.3, JPA, Security, JWT
- `backend/settings.gradle`
- `backend/src/main/resources/application.yml` — DB, JPA, JWT 설정

### DB 스키마/시드
- `backend/src/main/resources/schema.sql` — 10개 테이블 DDL (FK 포함)
- `backend/src/main/resources/data.sql` — 매장1, 관리자1, 테이블5, 메뉴10, 맵기옵션16

### JPA 엔티티 (10개)
- `common/entity/Store.java`
- `common/entity/Admin.java`
- `common/entity/StoreTable.java`
- `common/entity/TableSession.java`
- `common/entity/Menu.java`
- `common/entity/MenuSpicyOption.java`
- `common/entity/Order.java`
- `common/entity/OrderItem.java`
- `common/entity/OrderHistory.java`
- `common/entity/OrderHistoryItem.java`

### 설정 클래스
- `TableOrderApplication.java` — Spring Boot main
- `common/config/SecurityConfig.java` — permitAll (JWT는 Unit 2에서 추가)
- `common/config/WebConfig.java` — 프론트엔드 정적 리소스 서빙

### 인프라
- `docker-compose.yml` — app + db 서비스
- `backend/Dockerfile` — multi-stage build (JDK 17)

## 검증 결과
- ✅ Gradle 빌드 성공
- ✅ Docker Compose 기동 성공 (app + db)
- ✅ DB 시드 데이터 확인 (메뉴 10개, 테이블 5개)
- ✅ HTTP 응답 확인 (404 = 앱 정상 기동, 컨트롤러 미구현)
