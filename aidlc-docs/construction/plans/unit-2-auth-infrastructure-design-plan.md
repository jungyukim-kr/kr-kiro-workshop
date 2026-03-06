# Unit 2: 인증 모듈 - Infrastructure Design Plan

## Unit 정보
- **Unit 이름**: Unit 2 - 인증 모듈 (Authentication Module)
- **팀**: 공통 (양 팀 협업)

## 이미 결정된 인프라 사항 (Unit 1에서)
- 배포: Docker Compose (PostgreSQL + Spring Boot)
- DB: PostgreSQL
- 백엔드: Spring Boot 3.x
- 프론트엔드: Vanilla JS (정적 파일)

## Infrastructure Design 체크리스트

### 1. 인증 인프라 매핑
- [x] JWT Secret Key 관리 인프라
- [x] Spring Security 설정 인프라
- [x] 환경별 설정 분리

### 2. 배포 아키텍처
- [x] 인증 모듈 배포 구조 (모놀리식 내 패키지)
- [x] 환경변수 관리 방식

---

## 질문 섹션

### Q1: JWT Secret Key 환경변수 관리
Docker Compose 환경에서 JWT Secret Key를 어떻게 관리할까요?

A) docker-compose.yml의 environment에 직접 설정 (개발 편의)
B) .env 파일에 분리하여 관리 (.gitignore에 추가)
C) Docker Secrets 사용
D) application.yml에 기본값 설정, 환경변수로 오버라이드
E) Other (설명해주세요)

[Answer]:A

### Q2: Spring Boot 프로필 관리
환경별 설정 분리는 어떻게 할까요?

A) application.yml (기본) + application-prod.yml (운영)
B) application.yml (기본) + application-dev.yml (개발) + application-prod.yml (운영)
C) 단일 application.yml (환경변수로 분기)
D) 프로필 불필요 (MVP는 단일 환경)
E) Other (설명해주세요)

[Answer]:A

---

## 다음 단계
모든 질문에 답변하신 후, Infrastructure Design 산출물을 생성합니다.
