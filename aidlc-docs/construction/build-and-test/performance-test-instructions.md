# Performance Test Instructions

## 목적

인증 모듈의 NFR 성능 요구사항 충족 여부를 검증합니다.

## 성능 요구사항 (NFR-AUTH-01)

| 항목 | 목표 |
|------|------|
| 로그인 API 응답 시간 | < 1초 |
| JWT 토큰 검증 | < 10ms (서명 검증만) |
| 단일 세션 검증 (DB 포함) | < 100ms |
| 비밀번호 변경 API | < 1초 |
| 동시 접속 | 매장당 50개 테이블 동시 로그인 |

## 사전 조건

- Docker Compose로 앱 + DB 기동
- 시드 데이터 로드 완료
- 성능 테스트 도구 설치 (Apache Bench, k6, 또는 JMeter)

## 테스트 실행

### Test 1: 로그인 API 응답 시간

```bash
# Apache Bench로 관리자 로그인 100회 실행
ab -n 100 -c 10 -T "application/json" \
  -p login-payload.json \
  http://localhost:8080/api/auth/admin/login

# login-payload.json:
# {"storeCode":"STORE001","username":"admin","password":"admin123"}
```

기대 결과:
- 평균 응답 시간 < 500ms
- 99th percentile < 1000ms

### Test 2: 토큰 검증 API 응답 시간

```bash
# 먼저 토큰 발급
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE001","username":"admin","password":"admin123"}' \
  | jq -r '.token')

# 토큰 검증 1000회 실행
ab -n 1000 -c 50 \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/validate
```

기대 결과:
- 평균 응답 시간 < 50ms
- 99th percentile < 100ms

### Test 3: 동시 테이블 로그인

```bash
# k6 스크립트 (table-login-load.js)
# 50개 가상 사용자가 동시에 테이블 로그인 수행
```

```javascript
// table-login-load.js (k6)
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 50,
  duration: '30s',
};

export default function () {
  const tableNumber = Math.floor(Math.random() * 5) + 1;
  const payload = JSON.stringify({
    storeCode: 'STORE001',
    tableNumber: tableNumber,
    password: '1234',
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const res = http.post('http://localhost:8080/api/auth/table/login', payload, params);
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 1000ms': (r) => r.timings.duration < 1000,
  });

  sleep(0.5);
}
```

```bash
k6 run table-login-load.js
```

기대 결과:
- 성공률 > 99%
- 평균 응답 시간 < 500ms
- 에러율 < 1%

## 결과 분석

| 테스트 | 목표 | 실제 | 상태 |
|--------|------|------|------|
| 로그인 응답 시간 | < 1s | (측정 필요) | ⬜ |
| 토큰 검증 응답 시간 | < 100ms | (측정 필요) | ⬜ |
| 동시 50 테이블 로그인 | 성공률 > 99% | (측정 필요) | ⬜ |

## 참고

- MVP 단계에서는 단일 인스턴스 운영이므로 대규모 부하 테스트는 불필요
- bcrypt cost factor 10 기준 해싱 ~100ms가 로그인 응답의 주요 병목
- Spring Boot 기본 스레드 풀 (200)로 동시 50 요청 처리 충분
