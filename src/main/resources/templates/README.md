# Templates 디렉토리

이 디렉토리에는 서버 사이드 렌더링에 필요한 최소한의 HTML 템플릿만 포함되어 있습니다. 이 템플릿들은 점진적으로 분리된 프론트엔드 애플리케이션으로 마이그레이션될 예정입니다.

## 구조

- `auth/`: 인증 관련 템플릿
  - `token-display.html`: OAuth 인증 후 토큰 표시 페이지
  - `token-error.html`: 인증 오류 표시 페이지
  - `processing.html`: 인증 처리 중 페이지
- `error/`: 에러 페이지
  - `400.html`: 400 Bad Request 에러 페이지
  - `403.html`: 403 Forbidden 에러 페이지
  - `404.html`: 404 Not Found 에러 페이지

## 마이그레이션 계획

현재 이 Thymeleaf 템플릿들은 다음과 같은 마이그레이션 계획에 따라 단계적으로
SPA (Single Page Application) 기반 프론트엔드로 대체될 예정입니다:

1. **1단계 (현재)**: 필수적인 서버 렌더링 페이지 유지
   - OAuth 인증 콜백 처리 페이지 (`auth/token-display.html` 등)
   - 기본 에러 페이지 (`error/*.html`)

2. **2단계 (진행 중)**: 프론트엔드 예제 코드 개발
   - 프론트엔드 예제 코드는 `resources/frontend-examples/` 디렉토리에 개발 중
   - 현재 구현된 컴포넌트:
     - `OAuthCallback.jsx`: 인증 콜백 처리 React 컴포넌트
     - `ErrorHandler.jsx`: 에러 처리 React 컴포넌트

3. **3단계 (예정)**: 완전한 프론트엔드 애플리케이션으로 마이그레이션
   - 별도 리포지토리의 React/Vue 기반 프론트엔드 애플리케이션 개발
   - 백엔드는 순수 API 서버로 운영

## API 응답과 템플릿의 결합

이 프로젝트에서는 대부분의 데이터 교환은 JSON API를 통해 이루어지지만, 특정 경우에는 HTML 페이지가 필요합니다:

1. **OAuth 인증 콜백 처리**: 인증 제공자(카카오 등)로부터의 리다이렉션을 처리
2. **에러 페이지**: 사용자 친화적인 에러 메시지 표시

## 하이브리드 접근법

현재 구현은 Accept 헤더에 따라 다음과 같은 응답을 제공합니다:

- **Accept: application/json**: JSON 응답을 반환
- **Accept: text/html** (기본값): HTML 템플릿을 렌더링

이 방식으로 클라이언트 애플리케이션에 최대한의 유연성을 제공합니다.

## 프론트엔드 예제 코드 활용

`resources/frontend-examples/` 디렉토리에 있는 예제 코드를 사용하여 다음과 같이 프론트엔드 통합을 진행할 수 있습니다:

1. 해당 예제 코드를 별도의 React 프로젝트에 통합
2. 적절한 라우팅 설정으로 OAuth 콜백 처리 및 에러 처리 구현
3. 백엔드 API와의 통신을 위한 인증 토큰 관리 로직 구현 