# PleasyBank - 금융 서비스 애플리케이션

PleasyBank는 사용자 친화적인 인터페이스와 오픈뱅킹 API 연동을 통해 계좌 관리, 거래 처리, 금융 데이터 분석 기능을 제공하는 금융 서비스 애플리케이션입니다.

## 프로젝트 구조

PleasyBank는 다음과 같은 주요 모듈로 구성되어 있습니다:

- **인증 및 보안**: 카카오 소셜 로그인, JWT 기반 인증, 오픈뱅킹 연동
- **계좌 관리**: 오픈뱅킹 API를 활용한 계좌 정보 조회
- **거래 처리**: 오픈뱅킹 API를 통한 이체 및 거래 처리
- **데이터 분석**: 거래 내역 분석 및 시각화

## 설계 방향

PleasyBank는 금융결제원 오픈뱅킹 API에 70-80% 의존하는 형태로 설계되었습니다. 이를 통해 개발 리소스를 절약하고, 안정적인 금융 서비스를 제공할 수 있습니다. 자체 DB에는 주로 사용자 정보와 인증 정보를 저장하고, 계좌 및 거래 데이터는 오픈뱅킹 API를 통해 실시간으로 조회합니다.

## 주요 기능

### 인증
- 카카오 소셜 로그인
- JWT 기반 사용자 인증
- 오픈뱅킹 연동 및 토큰 관리

### 계좌 관리
- 사용자 계좌 목록 조회
- 계좌 상세 정보 조회
- 계좌 잔액 조회

### 거래 처리
- 계좌 이체
- 거래 내역 조회
- 일별/월별 거래 분석

## 기술 스택

- **언어**: Kotlin
- **프레임워크**: Spring Boot
- **보안**: Spring Security, OAuth2, JWT
- **데이터베이스**: H2(개발), PostgreSQL(운영)
- **API 문서화**: Swagger/OpenAPI
- **외부 연동**: 금융결제원 오픈뱅킹 API

## 필수 설정

### application.yml 설정
```yaml
openbanking:
  client-id: YOUR_CLIENT_ID
  client-secret: YOUR_CLIENT_SECRET
  base-url: https://testapi.openbanking.or.kr
  redirect-uri: http://localhost:8080/api/openbanking/callback
  auth-url: https://testapi.openbanking.or.kr/oauth/2.0/authorize
```

### 오픈뱅킹 API 연동
1. 금융결제원 오픈뱅킹 API 등록 및 클라이언트 정보 발급
2. `application.yml`에 클라이언트 정보 설정
3. 애플리케이션 실행 후 카카오 로그인으로 사용자 인증
4. 오픈뱅킹 인증 화면에서 계좌 접근 권한 허용

## 로컬 개발 환경 설정

1. JDK 17 설치
2. 프로젝트 클론
```
git clone https://github.com/yourusername/pleasybank.git
```
3. 애플리케이션 실행
```
./gradlew bootRun
```
4. 애플리케이션 접속
```
http://localhost:8080
```

## API 문서
Swagger UI를 통해 API 문서를 확인할 수 있습니다.
```
http://localhost:8080/swagger-ui/index.html
``` 