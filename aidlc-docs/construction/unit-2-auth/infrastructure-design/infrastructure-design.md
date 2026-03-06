# Unit 2: 인증 모듈 - Infrastructure Design

## 인프라 개요

Unit 2 인증 모듈은 Unit 1에서 구성한 Docker Compose 인프라 위에서 동작합니다.
별도의 인프라 추가 없이, Spring Boot 모놀리식 앱 내 패키지로 배포됩니다.

---

## 1. 배포 구조

### 모놀리식 내 패키지 구조
```
Spring Boot App (단일 JAR)
+-- com.tableorder.common/    # Unit 1
+-- com.tableorder.auth/      # Unit 2 (이 유닛)
+-- com.tableorder.customer/  # Unit 3 (향후)
+-- com.tableorder.admin/     # Unit 5 (향후)
```

- 인증 모듈은 독립 서비스가 아닌 Spring Boot 앱 내 패키지
- 모든 유닛이 동일한 JVM 프로세스에서 실행
- 컴포넌트 간 통신은 내부 메서드 호출

---

## 2. JWT Secret Key 관리

### docker-compose.yml 설정
```yaml
services:
  app:
    environment:
      JWT_SECRET: "your-256-bit-secret-key-here-min-32-chars"
```

### application.yml 매핑
```yaml
app:
  jwt:
    secret: ${JWT_SECRET:default-dev-secret-key-min-32-characters-long}
    expiration: 57600        # 16시간 (초)
    refresh-threshold: 28800 # 8시간 (초)
```

- 개발 환경: application.yml의 기본값 사용
- Docker 환경: docker-compose.yml의 environment로 오버라이드
- 운영 환경: 환경변수로 강력한 키 설정

---

## 3. Spring Boot 프로필 관리

### 프로필 구조

| 파일 | 용도 | 활성화 |
|------|------|--------|
| application.yml | 기본 설정 (개발) | 항상 |
| application-prod.yml | 운영 설정 오버라이드 | `SPRING_PROFILES_ACTIVE=prod` |

### application.yml (기본/개발)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tableorder
    username: tableorder
    password: tableorder

app:
  jwt:
    secret: ${JWT_SECRET:default-dev-secret-key-min-32-characters-long}
    expiration: 57600
    refresh-threshold: 28800
  cors:
    allowed-origins: "*"

logging:
  level:
    com.tableorder.auth: INFO
    org.springframework.security: WARN
```

### application-prod.yml (운영)
```yaml
app:
  cors:
    allowed-origins: "https://your-domain.com"

logging:
  level:
    com.tableorder.auth: INFO
    org.springframework.security: ERROR
```

### Docker Compose에서 프로필 활성화
```yaml
services:
  app:
    environment:
      SPRING_PROFILES_ACTIVE: prod  # 운영 시
      JWT_SECRET: "strong-production-secret-key"
```

---

## 4. Spring Security 인프라 설정

### SecurityFilterChain 설정 요약

| 경로 | 접근 제어 | 필터 |
|------|-----------|------|
| `/api/auth/admin/login` | permitAll | - |
| `/api/auth/table/login` | permitAll | - |
| `/api/auth/admin/password` | ADMIN | JwtFilter + StoreFilter |
| `/api/auth/validate` | authenticated | JwtFilter |
| `/api/stores/{id}/admin/**` | ADMIN | JwtFilter + StoreFilter |
| `/api/stores/{id}/customer/**` | TABLE | JwtFilter + StoreFilter |
| `/api/stores/{id}/events` | ADMIN | JwtFilter + StoreFilter |

### 비활성화 항목
- CSRF: 비활성화 (stateless REST API)
- Session: STATELESS
- FormLogin: 비활성화
- HttpBasic: 비활성화

---

## 5. 데이터베이스 변경사항

### Admin 테이블 추가 컬럼 (Unit 1 스키마 대비)
```sql
ALTER TABLE admin ADD COLUMN last_token_issued_at TIMESTAMP;
```

- schema.sql에 포함하여 Unit 1 스키마와 함께 초기화
- 관리자 단일 세션 검증용

---

## 6. Gradle 의존성 (Unit 2 추가분)

```groovy
dependencies {
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT (jjwt)
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    
    // Test
    testImplementation 'org.springframework.security:spring-security-test'
}
```
