package com.pleasybank.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * JWT 설정 클래스
 */
@Configuration
@PropertySource("classpath:application.yml")
class JwtConfig {

    @Bean
    @ConfigurationProperties(prefix = "jwt")
    fun jwtProperties(): JwtProperties {
        return JwtProperties()
    }
}

/**
 * JWT 속성 클래스
 */
class JwtProperties {
    lateinit var secretKey: String
    var accessTokenExpirationMs: Long = 0
    var refreshTokenExpirationMs: Long = 0
} 