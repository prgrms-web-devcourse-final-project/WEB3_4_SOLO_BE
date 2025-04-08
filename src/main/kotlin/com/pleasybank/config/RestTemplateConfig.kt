package com.pleasybank.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate
import java.time.Duration
import org.springframework.web.client.RestClient

/**
 * RestTemplate 구성 클래스
 * 외부 API 호출에 사용할 RestTemplate 빈을 설정합니다.
 */
@Configuration
class RestTemplateConfig {
    
    @Bean
    @Primary
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .build()
    }

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .build()
    }
} 