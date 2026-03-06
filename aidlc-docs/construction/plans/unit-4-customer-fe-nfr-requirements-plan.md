# NFR Requirements Plan - Unit 4: 고객 프론트엔드

## Unit Context
- **Unit**: unit-4-customer-fe
- **기술 스택**: Vanilla JS + HTML + CSS + Bootstrap 5 CDN
- **배포 방식**: 정적 파일 (Spring Boot static resources 또는 별도 웹서버)
- **의존**: Unit 2 (Auth API), Unit 3 (Customer API)

## Plan Steps

- [x] 1. Functional Design 산출물 분석 (화면 구조, 데이터 흐름)
- [x] 2. 사용자 질문 수집 및 답변 분석
- [x] 3. NFR Requirements 문서 생성
- [x] 4. Tech Stack Decisions 문서 생성

---

## 질문

### 성능 요구사항

**Q1**: 정적 파일 배포 방식은?

A) Spring Boot의 `static/` 디렉토리에 포함 (backend와 동일 서버)
B) 별도 Nginx/Apache 웹서버
C) S3 + CloudFront (CDN)

[Answer]:A

### 브라우저 호환성

**Q2**: 지원 브라우저 범위는?

A) 최신 Chrome만 (태블릿 전용 키오스크 모드)
B) Chrome + Safari (태블릿 주요 브라우저)
C) 모든 최신 브라우저 (Chrome, Safari, Firefox, Edge)

[Answer]:C

### 오프라인/네트워크

**Q3**: 네트워크 불안정 시 대응 수준은?

A) 기본 에러 메시지만 표시 (현재 설계 수준)
B) 재시도 로직 + 오프라인 감지 배너
C) Service Worker 기반 오프라인 캐싱

[Answer]:A

### 접근성

**Q4**: 접근성(Accessibility) 수준은?

A) 기본 수준 (시맨틱 HTML, alt 속성 정도)
B) WCAG 2.1 AA 준수 목표

[Answer]:A

</content>
</invoke>