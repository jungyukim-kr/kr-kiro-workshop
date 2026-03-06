# Requirements Verification Questions

요구사항 문서를 분석한 결과, 다음 사항들에 대한 확인이 필요합니다.
각 질문의 [Answer]: 태그 뒤에 선택한 옵션 문자를 입력해주세요.

## Question 1
백엔드 기술 스택으로 어떤 것을 사용하시겠습니까?

A) Node.js (Express/Fastify)
B) Python (FastAPI/Django)
C) Java (Spring Boot)
D) Go
E) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 2
프론트엔드 기술 스택으로 어떤 것을 사용하시겠습니까?

A) React (Vite)
B) Next.js
C) Vue.js
D) Vanilla HTML/CSS/JavaScript
E) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 3
데이터베이스로 어떤 것을 사용하시겠습니까?

A) PostgreSQL
B) MySQL
C) Amazon DynamoDB
D) SQLite (개발/프로토타입용)
E) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
배포 환경은 어디를 대상으로 하시겠습니까?

A) AWS (EC2, ECS, Lambda 등)
B) 로컬 서버 / Docker Compose
C) Vercel/Netlify (프론트) + AWS (백엔드)
D) 배포는 고려하지 않음 (로컬 개발 환경만)
E) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 5
고객용 인터페이스와 관리자용 인터페이스를 어떻게 구성하시겠습니까?

A) 하나의 프론트엔드 앱에서 라우팅으로 분리
B) 별도의 프론트엔드 앱 2개 (고객용, 관리자용)
C) 관리자는 별도 앱, 고객용은 서버 사이드 렌더링
D) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 6
메뉴 이미지 관리는 어떻게 하시겠습니까? (요구사항에 이미지 URL이 포함되어 있습니다)

A) 외부 이미지 URL만 지원 (직접 업로드 없음)
B) 로컬 파일 시스템에 이미지 업로드
C) AWS S3 등 클라우드 스토리지에 업로드
D) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 7
매장(Store) 데이터는 어떻게 관리하시겠습니까?

A) 단일 매장만 지원 (MVP 단순화)
B) 다중 매장 지원 (매장별 독립 운영)
C) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 8
관리자 계정 관리는 어떻게 하시겠습니까?

A) 사전 설정된 관리자 계정 (시드 데이터)
B) 관리자 회원가입 기능 포함
C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 9
프로젝트 구조를 어떻게 구성하시겠습니까?

A) 모노레포 (프론트엔드 + 백엔드 하나의 저장소)
B) 백엔드만 구현 (프론트엔드는 별도)
C) 풀스택 프레임워크 사용 (Next.js 등)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 10
테이블 태블릿의 자동 로그인 정보 저장 방식은 어떻게 하시겠습니까?

A) 브라우저 localStorage 사용
B) 브라우저 쿠키 사용
C) Other (please describe after [Answer]: tag below)

[Answer]: A
