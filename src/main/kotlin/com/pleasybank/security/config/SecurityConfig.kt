package com.pleasybank.security.config

import com.pleasybank.security.jwt.JwtAuthenticationFilter
import com.pleasybank.security.jwt.JwtTokenProvider
import com.pleasybank.security.oauth2.CustomOAuth2UserService
import com.pleasybank.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import com.pleasybank.security.oauth2.OAuth2AuthenticationSuccessHandler
import com.pleasybank.security.oauth2.OAuth2AuthenticationFailureHandler
import com.pleasybank.security.oauth2.CustomOAuth2AuthorizationRequestResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.slf4j.LoggerFactory

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler,
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    private val customOAuth2AuthorizationRequestResolver: CustomOAuth2AuthorizationRequestResolver
) {
    
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> 
                auth
                    // 인증 관련 경로
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/login/**").permitAll()
                    .requestMatchers("/oauth2/**").permitAll()
                    .requestMatchers("/auth/oauth2/**").permitAll()
                    .requestMatchers("/login/oauth2/code/*").permitAll()
                    .requestMatchers("/auth/reset-password-request", "/auth/reset-password").permitAll()
                    .requestMatchers("/auth/biometric/setup").permitAll()
                    .requestMatchers("/auth/token-display").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    .requestMatchers("/api/test/**").permitAll()
                    
                    // 오픈뱅킹 API 인증 관련 경로
                    .requestMatchers("/api/openbanking/auth").permitAll()
                    .requestMatchers("/api/openbanking/auth/**").permitAll()
                    .requestMatchers("/api/openbanking/callback").permitAll()
                    
                    // H2 콘솔
                    .requestMatchers("/h2-console/**").permitAll()
                    
                    // Swagger UI와 API 문서
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    .requestMatchers("/api/swagger-ui.html").permitAll()
                    .requestMatchers("/api/swagger-ui/**").permitAll()
                    .requestMatchers("/api/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/api-docs/**").permitAll()
                    .requestMatchers("/api-docs/swagger-config").permitAll()
                    .requestMatchers("/v3/api-docs/swagger-config").permitAll()
                    
                    // 시스템 상태 체크
                    .requestMatchers("/system/health", "/system/version", "/system/test").permitAll()
                    
                    // 특정 OPTIONS 요청 허용 (CORS preflight)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    
                    // 나머지 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            .sessionManagement { 
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                it.invalidSessionUrl("/auth/token-display?error=invalid_session")
                it.maximumSessions(3)
                    .expiredUrl("/auth/token-display?error=session_expired")
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { endpoint -> 
                        endpoint.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        endpoint.baseUri("/oauth2/authorize")
                    }
                    .redirectionEndpoint {
                        it.baseUri("/login/oauth2/code/*")
                    }
                    .userInfoEndpoint { endpoint ->
                        endpoint.userService(customOAuth2UserService)
                    }
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)
            
        // H2 콘솔을 위한 설정
        http
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            
        return http.build()
    }
} 