# Unit 3: Customer Backend - Infrastructure Design

## 인프라 변경사항: 없음

Unit 3은 Unit 1에서 구성된 기존 인프라(Docker Compose: app + db) 위에서 동작합니다.
추가 컨테이너, 서비스, 포트, 볼륨 변경이 필요하지 않습니다.

## 기존 인프라 활용

| 항목 | 설정 | 비고 |
|------|------|------|
| 앱 서버 | app 컨테이너 (:8080) | 동일 Spring Boot 앱에 코드 추가 |
| DB | db 컨테이너 (:5432) | 기존 스키마 그대로 사용 |
| 인증 | Unit 2 Security Filter Chain | JWT 필터가 요청 전 자동 처리 |

## API 경로 매핑

```
http://localhost:8080/api/stores/{storeId}/customer/categories
http://localhost:8080/api/stores/{storeId}/customer/menus?category=
http://localhost:8080/api/stores/{storeId}/customer/menus/{menuId}/spicy-options
http://localhost:8080/api/stores/{storeId}/customer/orders
```

- Unit 2의 StoreAccessFilter가 `/api/stores/{storeId}/customer/**` 패턴에 대해 TABLE 역할 + 매장 격리 검증

## build.gradle 변경사항: 없음

Unit 1에서 추가한 의존성(spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security)으로 충분합니다.
