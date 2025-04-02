package com.pleasybank.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.reactive.function.client.WebClient
import org.slf4j.LoggerFactory

@Configuration
class WebConfig : WebMvcConfigurer {
    private val logger = LoggerFactory.getLogger(WebConfig::class.java)

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        logger.info("CORS 설정 적용 중...")
        
        // 모든 API에 대한 기본 CORS 설정
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:3001") // 와일드카드(*) 대신 구체적인 오리진 지정
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true) // credentials 허용
            .maxAge(3600)
            
        // 카카오 인증 엔드포인트에 대한 특별 설정
        registry.addMapping("/api/auth/kakao")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("POST", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization")
            .allowCredentials(true)
            .maxAge(3600)
            
        logger.info("CORS 설정 완료")
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .build()
    }
} 