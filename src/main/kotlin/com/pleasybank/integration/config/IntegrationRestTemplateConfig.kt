package com.pleasybank.integration.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate
import java.time.Duration

/**
 * Integration API 호출용 RestTemplate 구성 클래스
 * 외부 API 호출에 사용할 RestTemplate 빈을 설정합니다.
 */
@Configuration
class IntegrationRestTemplateConfig {
    
    @Bean
    @Primary
    fun integrationRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .build()
    }
} 