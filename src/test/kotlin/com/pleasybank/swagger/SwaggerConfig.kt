package com.pleasybank.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger UI 설정
 * 해당 설정은 테스트 용도로만 사용됩니다.
 * 프로덕션에서는 직접 적용하지 않습니다.
 */
@Configuration
class SwaggerConfig {
    
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"
        val openBankingSecurityName = "openBankingAuth"
        
        val server = Server()
            .url("/")
            .description("로컬 서버")
        
        val openBankingTag = Tag()
            .name("오픈뱅킹 API")
            .description("금융결제원 오픈뱅킹 API 연동 관련 엔드포인트")
        
        return OpenAPI()
            .info(apiInfo())
            .addServersItem(server)
            .addTagsItem(openBankingTag)
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .addSecurityItem(SecurityRequirement().addList(openBankingSecurityName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
                    .addSecuritySchemes(
                        openBankingSecurityName,
                        SecurityScheme()
                            .name(openBankingSecurityName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("oauth2")
                            .description("금융결제원 오픈뱅킹 API 액세스 토큰")
                    )
            )
    }
    
    private fun apiInfo() = Info()
        .title("PleasyBank API")
        .description("""
            PleasyBank 애플리케이션의 API 문서
            
            ## 오픈뱅킹 API 사용 방법
            
            1. `/api/openbanking/auth` 엔드포인트를 호출하여 금융결제원 인증 페이지로 이동
            2. 인증 후 콜백으로 받은 access_token을 이용하여 API 호출
            3. 모든 API 요청 시 Authorization 헤더에 Bearer 토큰 포함 필요
            
            ## 주요 기능
            - 계좌 목록 조회
            - 계좌 잔액 조회
            - 거래 내역 조회
            - 계좌 이체
        """)
        .version("1.0.0")
        .contact(
            Contact()
                .name("PleasyBank Team")
                .email("support@pleasybank.com")
                .url("https://pleasybank.com")
        )
        .license(
            License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
        )
} 