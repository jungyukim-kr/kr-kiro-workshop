# Build Instructions

## Prerequisites

| 항목 | 요구사항 |
|------|----------|
| JDK | OpenJDK 17 |
| Build Tool | Gradle 8.14 (Wrapper 포함) |
| Database | PostgreSQL 15+ (Docker Compose 제공) |
| Docker | Docker Desktop (docker-compose 포함) |
| OS | Windows / macOS / Linux |

## Environment Variables

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `JAVA_HOME` | - | JDK 17 경로 (시스템 기본이 17이 아닌 경우 필수) |
| `JWT_SECRET` | `default-dev-secret-key-min-32-characters-long` | JWT 서명 키 (운영 시 반드시 변경) |
| `SPRING_PROFILES_ACTIVE` | (없음 = 개발) | `prod` 설정 시 운영 프로필 활성화 |

## Build Steps

### 1. 데이터베이스 기동

```bash
docker-compose up -d db
```

PostgreSQL이 `localhost:5432`에서 기동됩니다.
- DB: `tableorder`
- User: `tableorder`
- Password: `tableorder`

### 2. 의존성 설치 및 빌드

```bash
# Windows (JAVA_HOME이 17이 아닌 경우)
$env:JAVA_HOME = "D:\DevLib\openjdk-17.0.0.1"
cd backend
.\gradlew.bat build

# macOS/Linux
cd backend
./gradlew build
```

### 3. 빌드 결과 확인

- 빌드 성공 시: `BUILD SUCCESSFUL`
- JAR 위치: `backend/build/libs/backend-0.0.1-SNAPSHOT.jar`
- 테스트 리포트: `backend/build/reports/tests/test/index.html`

### 4. 애플리케이션 실행

```bash
# 직접 실행
java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar

# Docker Compose로 전체 실행
docker-compose up -d
```

- 앱: `http://localhost:8080`
- DB: `localhost:5432`

## Troubleshooting

### Gradle 빌드 시 JDK 버전 오류
- 원인: 시스템 JAVA_HOME이 JDK 17이 아닌 경우
- 해결: `$env:JAVA_HOME = "JDK17경로"` 설정 후 빌드

### PostgreSQL 연결 실패
- 원인: Docker DB가 기동되지 않음
- 해결: `docker-compose up -d db` 실행 후 재시도

### 테스트 실패 시
- 테스트 리포트 확인: `backend/build/reports/tests/test/index.html`
- 개별 테스트 실행: `.\gradlew.bat test --tests "com.tableorder.auth.controller.AuthControllerTest"`
