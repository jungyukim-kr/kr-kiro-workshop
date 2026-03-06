# Code Summary - Unit 2: 인증 모듈

## 생성 방식
- TDD (Test-Driven Development) - RED-GREEN-REFACTOR 사이클

## 패키지 구조

```
backend/src/main/java/com/tableorder/auth/
├── controller/
│   └── AuthController.java              # REST API 엔드포인트 (4개)
├── dto/
│   ├── AuthInfo.java                    # 내부 인증 정보 DTO
│   ├── ChangePasswordRequest.java       # 비밀번호 변경 요청 DTO
│   ├── ErrorResponse.java               # 에러 응답 DTO
│   ├── LoginRequest.java                # 관리자 로그인 요청 DTO
│   ├── MessageResponse.java             # 메시지 응답 DTO
│   ├── TableLoginRequest.java           # 테이블 로그인 요청 DTO
│   ├── TokenResponse.java               # 토큰 응답 DTO
│   └── ValidateResponse.java            # 토큰 검증 응답 DTO
├── exception/
│   ├── AuthException.java               # 인증 예외 (errorCode + message)
│   └── GlobalExceptionHandler.java      # 전역 예외 처리 (@RestControllerAdvice)
├── repository/
│   ├── AdminRepository.java             # 관리자 Repository
│   ├── StoreRepository.java             # 매장 Repository
│   ├── StoreTableRepository.java        # 테이블 Repository
│   └── TableSessionRepository.java      # 테이블 세션 Repository
├── security/
│   ├── CustomAccessDeniedHandler.java   # 403 JSON 응답 핸들러
│   ├── CustomAuthenticationEntryPoint.java # 401 JSON 응답 핸들러
│   ├── JwtAuthenticationFilter.java     # JWT 인증 필터 (단일 세션 + 자동 갱신)
│   ├── JwtTokenProvider.java            # JWT 토큰 생성/검증/갱신
│   ├── SecurityConfig.java              # Spring Security 필터 체인 설정
│   └── StoreAccessFilter.java           # 매장 격리 필터
└── service/
    └── AuthService.java                 # 인증 비즈니스 로직
```

## 테스트 구조

```
backend/src/test/java/com/tableorder/auth/
├── controller/
│   └── AuthControllerTest.java          # API Layer 테스트 (8개 TC)
├── security/
│   ├── JwtTokenProviderTest.java        # JWT 토큰 테스트 (8개 TC)
│   ├── JwtAuthenticationFilterTest.java # 인증 필터 테스트 (3개 TC)
│   └── StoreAccessFilterTest.java       # 매장 격리 필터 테스트 (2개 TC)
└── service/
    └── AuthServiceTest.java             # 비즈니스 로직 테스트 (11개 TC)
```

## 테스트 결과 요약
- 총 테스트 케이스: 32개
- 통과: 32개 (100%)
- 실패: 0개

## API 엔드포인트

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/auth/admin/login | 관리자 로그인 | 불필요 |
| POST | /api/auth/table/login | 테이블 로그인 | 불필요 |
| PUT | /api/auth/admin/password | 비밀번호 변경 | ROLE_ADMIN |
| GET | /api/auth/validate | 토큰 유효성 확인 | authenticated |

## 주요 구현 사항

### 인증 흐름
1. 로그인 요청 → AuthController → AuthService → JWT 토큰 발급
2. 인증된 요청 → JwtAuthenticationFilter → 토큰 검증 → SecurityContext 설정
3. 매장 격리 → StoreAccessFilter → URL storeId vs 토큰 storeId 비교

### 보안 기능
- JWT 토큰 기반 Stateless 인증
- 관리자 단일 세션 관리 (last_token_issued_at 비교)
- 토큰 자동 갱신 (잔여시간 < 8시간 시 X-New-Token 헤더)
- 매장 격리 (StoreAccessFilter)
- BCrypt 비밀번호 해싱
- 입력값 검증 (@Valid + Jakarta Validation)
- 전역 예외 처리 (GlobalExceptionHandler)

### Unit 1 수정 사항
- `schema.sql`: admin 테이블에 `last_token_issued_at` 컬럼 추가
- `application.yml`: JWT refresh-threshold, CORS 설정 추가
- `Admin.java`: `lastTokenIssuedAt` 필드 추가
- `build.gradle`: validation, test, JWT 의존성 추가
- `SecurityConfig.java`: common 패키지에서 auth 패키지로 이동 및 교체

## 빌드 확인
- Gradle build: SUCCESS
- 전체 테스트: 32/32 PASSED
