# Swagger 문서화 도구 (테스트 전용)

이 디렉토리에는 API 문서화를 위한 Swagger 설정 파일들이 포함되어 있습니다.
이 코드들은 **테스트 환경에서만 사용**되며, 프로덕션 환경에서는 직접 사용하지 않습니다.

## 사용 방법

테스트 환경에서 API 문서를 확인하려면:

1. 애플리케이션 실행 시 `spring.profiles.active=test` 프로필을 사용하여 실행합니다.
2. 브라우저에서 `http://localhost:8080/swagger-ui.html` 접속

## 주요 파일

- `SwaggerConfig.kt`: Swagger UI 기본 설정 및 인증 방식 정의
- `ApiDocumentationController.kt`: API 엔드포인트 문서 커스터마이징

## 프로덕션 환경 설정

프로덕션 환경에서는 보안상의 이유로 해당 기능이 비활성화됩니다. 
필요한 경우 별도의 문서 생성 및 배포 프로세스를 통해 API 문서를 관리하는 것을 권장합니다. 