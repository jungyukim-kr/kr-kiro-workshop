# Unit of Work - 정의

## 팀 구성
- **고객팀**: 고객 대면 기능 (백엔드 + 프론트엔드)
- **관리자팀**: 관리자 대면 기능 (백엔드 + 프론트엔드)

## 프로젝트 구조 (모노레포)

```
kr-kiro-workshop/
├── backend/                        # Spring Boot 앱
│   ├── src/main/java/
│   │   └── com/tableorder/
│   │       ├── common/             # Unit 1 (공통)
│   │       │   ├── config/
│   │       │   ├── entity/
│   │       │   └── security/
│   │       ├── auth/               # Unit 2 (공통 - 인증)
│   │       ├── customer/           # Unit 3 (고객팀 백엔드)
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   └── dto/
│   │       └── admin/              # Unit 5 (관리자팀 백엔드)
│   │           ├── controller/
│   │           ├── service/
│   │           └── dto/
│   ├── src/main/resources/
│   │   ├── application.yml         # Unit 1
│   │   ├── schema.sql              # Unit 1
│   │   └── data.sql                # Unit 1
│   └── build.gradle                # Unit 1
├── frontend/
│   ├── customer/                   # Unit 4 (고객팀 프론트엔드)
│   │   ├── index.html
│   │   ├── css/
│   │   └── js/
│   └── admin/                      # Unit 6 (관리자팀 프론트엔드)
│       ├── index.html
│       ├── css/
│       └── js/
├── docker-compose.yml              # Unit 1
└── aidlc-docs/
```

---

## Unit 1: DB 스키마 + 공통 설정 (공통)
- **팀**: 공통 (양 팀 협업)
- **범위**: PostgreSQL 스키마, Spring Boot 설정, Docker Compose, 시드 데이터, 공통 엔티티, Security 설정
- **산출물**: docker-compose.yml, application.yml, schema.sql, data.sql, build.gradle, 공통 엔티티/설정
- **구현 순서**: 1번째

## Unit 2: 인증 모듈 (공통)
- **팀**: 공통 (양 팀 협업)
- **범위**: 관리자 로그인, 테이블 태블릿 인증, JWT 발급/검증, bcrypt, 로그인 시도 제한
- **산출물**: AuthController, AuthService, Security Config
- **구현 순서**: 2번째

## Unit 3: 고객 백엔드 (고객팀)
- **팀**: 고객팀
- **범위**: 고객용 메뉴 조회 API, 주문 생성 API, 주문 내역 조회 API, 장바구니 관련 없음(클라이언트 전용)
- **산출물**: CustomerMenuController, CustomerOrderController, Service, DTO
- **구현 순서**: 3번째

## Unit 4: 고객 프론트엔드 (고객팀)
- **팀**: 고객팀
- **범위**: 자동 로그인, 메뉴 조회, 장바구니(localStorage), 주문 생성(요청사항/맵기), 주문 내역
- **산출물**: HTML, CSS, JavaScript
- **구현 순서**: 4번째 (Unit 3 API 완성 후)

## Unit 5: 관리자 백엔드 (관리자팀)
- **팀**: 관리자팀
- **범위**: 메뉴 CRUD API, 주문 모니터링 API, 주문 상태 변경/삭제 API, 테이블 관리 API, 세션 관리 API, 과거 이력 API, SSE 이벤트
- **산출물**: AdminMenuController, AdminOrderController, AdminTableController, SSEController, Service, DTO
- **구현 순서**: 3번째 (Unit 3과 병렬 가능)

## Unit 6: 관리자 프론트엔드 (관리자팀)
- **팀**: 관리자팀
- **범위**: 관리자 로그인, 실시간 대시보드(SSE), 주문 상태 변경, 테이블 관리, 과거 내역, 메뉴 관리
- **산출물**: HTML, CSS, JavaScript
- **구현 순서**: 4번째 (Unit 5 API 완성 후)
