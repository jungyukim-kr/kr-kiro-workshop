# Unit Test Execution

## TDD 아티팩트 확인

Unit 2 (인증 모듈)는 TDD 방식으로 개발되었으며, 모든 unit test가 Code Generation 단계에서 이미 실행 및 통과되었습니다.

- Test Plan: `aidlc-docs/construction/plans/unit-2-auth-test-plan.md`
- Contracts: `aidlc-docs/construction/plans/unit-2-auth-contracts.md`
- TDD Plan: `aidlc-docs/construction/plans/unit-2-auth-tdd-code-generation-plan.md`

## 테스트 현황

### Unit 1: DB/Common
- unit test 없음 (엔티티, 설정, 스키마만 포함)

### Unit 2: 인증 모듈 (TDD - 모두 통과 완료)

| 테스트 클래스 | TC 수 | 상태 |
|--------------|-------|------|
| JwtTokenProviderTest | 8 | ✅ ALL PASSED |
| AuthServiceTest | 11 | ✅ ALL PASSED |
| JwtAuthenticationFilterTest | 3 | ✅ ALL PASSED |
| StoreAccessFilterTest | 2 | ✅ ALL PASSED |
| AuthControllerTest | 8 | ✅ ALL PASSED |
| **합계** | **32** | **✅ ALL PASSED** |

## 검증 실행 (최종 변경 후 확인용)

```bash
# Windows
$env:JAVA_HOME = "D:\DevLib\openjdk-17.0.0.1"
cd backend
.\gradlew.bat test --no-daemon

# macOS/Linux
cd backend
./gradlew test --no-daemon
```

### 기대 결과
- 32개 테스트 모두 통과
- `BUILD SUCCESSFUL`
- 테스트 리포트: `backend/build/reports/tests/test/index.html`

## 개별 테스트 클래스 실행

```bash
# JwtTokenProvider 테스트
.\gradlew.bat test --tests "com.tableorder.auth.security.JwtTokenProviderTest"

# AuthService 테스트
.\gradlew.bat test --tests "com.tableorder.auth.service.AuthServiceTest"

# Security Filter 테스트
.\gradlew.bat test --tests "com.tableorder.auth.security.JwtAuthenticationFilterTest"
.\gradlew.bat test --tests "com.tableorder.auth.security.StoreAccessFilterTest"

# AuthController 테스트
.\gradlew.bat test --tests "com.tableorder.auth.controller.AuthControllerTest"
```

## 테스트 커버리지 영역

| 영역 | 커버리지 |
|------|----------|
| JWT 토큰 생성/검증/갱신 | TC-AUTH-001~008 |
| 관리자 로그인 (성공/실패 5가지) | TC-AUTH-009~013 |
| 테이블 로그인 (성공/실패 4가지) | TC-AUTH-014~017 |
| 비밀번호 변경 (성공/실패) | TC-AUTH-018~019 |
| API 엔드포인트 (4개 × 성공/실패) | TC-AUTH-020~027 |
| 인증 필터 (토큰 검증/단일 세션/갱신) | TC-AUTH-028~030 |
| 매장 격리 필터 (차단/통과) | TC-AUTH-031~032 |
