# Unit 2: 인증 모듈 - Tech Stack Decisions

## 기술 스택 결정 요약

| 영역 | 기술 | 버전 | 선정 사유 |
|------|------|------|-----------|
| JWT | jjwt (io.jsonwebtoken) | 0.12.x | 가장 널리 사용, 안정적, 풍부한 문서 |
| Security Framework | Spring Security 6.x | Spring Boot 3.x 포함 | SecurityFilterChain Bean 방식, 공식 권장 |
| Password Hashing | Spring Security Crypto (BCryptPasswordEncoder) | Spring Security 포함 | bcrypt cost factor 10, Spring 통합 |
| Logging | SLF4J + Logback | Spring Boot 기본 | 표준 로깅, 추가 의존성 불필요 |

---

## 상세 결정

### 1. JWT 라이브러리: jjwt

**선정**: `io.jsonwebtoken:jjwt-api` + `jjwt-impl` + `jjwt-jackson`

**Gradle 의존성**:
```groovy
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

**사용 방식**:
- 토큰 생성: `Jwts.builder()` API
- 토큰 파싱: `Jwts.parser()` API
- 서명: HS256 + SecretKey

---

### 2. Spring Security 설정

**방식**: SecurityFilterChain Bean (Spring Security 6.x 권장)

**구성 요소**:
- `SecurityConfig`: SecurityFilterChain Bean 정의
- `JwtAuthenticationFilter`: OncePerRequestFilter 상속, JWT 검증
- `JwtTokenProvider`: 토큰 생성/검증 유틸리티

**필터 체인 순서**:
```
HTTP Request
  → CorsFilter (Spring 기본)
  → JwtAuthenticationFilter (커스텀)
  → AuthorizationFilter (Spring Security)
  → Controller
```

**SecurityFilterChain 설정**:
- `/api/auth/**`: permitAll (로그인 엔드포인트)
- `/api/stores/{storeId}/admin/**`: ADMIN 역할 필요
- `/api/stores/{storeId}/customer/**`: TABLE 역할 필요
- 그 외: authenticated

---

### 3. 비밀번호 해싱

**구현**: `BCryptPasswordEncoder` (Spring Security 내장)

**설정**:
- Cost Factor: 10 (기본값)
- Bean 등록: `@Bean BCryptPasswordEncoder passwordEncoder()`

---

### 4. CORS 설정

**구현**: Spring Security CorsConfigurationSource

**프로필별 설정**:
- `application.yml` (개발):
  ```yaml
  app:
    cors:
      allowed-origins: "*"
  ```
- `application-prod.yml` (운영):
  ```yaml
  app:
    cors:
      allowed-origins: "https://your-domain.com"
  ```

**노출 헤더**: `X-New-Token` (토큰 자동 갱신용)

---

### 5. 에러 처리

**구현**: `@RestControllerAdvice` + `@ExceptionHandler`

**에러 응답 DTO**:
```java
public record ErrorResponse(
    String error,
    String message,
    String timestamp
) {}
```

**커스텀 예외 클래스**:
- `AuthenticationException`: 인증 실패 (401)
- `AccessDeniedException`: 접근 거부 (403)
- `ValidationException`: 입력 검증 실패 (400)

---

### 6. 로깅 설정

**구현**: SLF4J + Logback (Spring Boot 기본)

**로깅 레벨 설정** (application.yml):
```yaml
logging:
  level:
    com.tableorder.auth: INFO
    org.springframework.security: WARN
```

---

## 프로젝트 의존성 요약 (Unit 2 추가분)

```groovy
// build.gradle - Unit 2에서 추가되는 의존성
dependencies {
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    
    // Test
    testImplementation 'org.springframework.security:spring-security-test'
}
```
