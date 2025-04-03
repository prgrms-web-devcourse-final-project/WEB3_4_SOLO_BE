package com.pleasybank.core.config

import com.pleasybank.core.security.filter.JwtTokenFilter
import com.pleasybank.core.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import com.pleasybank.core.security.oauth2.OAuth2AuthenticationFailureHandler
import com.pleasybank.core.security.oauth2.OAuth2AuthenticationSuccessHandler
import com.pleasybank.core.security.oauth2.service.OAuth2UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.slf4j.LoggerFactory

@Configuration
@EnableWebSecurity
class SecurityConfig {
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)
    
    @Autowired
    private lateinit var jwtTokenFilter: JwtTokenFilter
    
    @Autowired
    private lateinit var oAuth2UserService: OAuth2UserService
    
    @Autowired
    private lateinit var oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler
    
    @Autowired
    private lateinit var oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler
    
    @Autowired
    private lateinit var httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        logger.info("BCryptPasswordEncoder 빈 생성")
        return BCryptPasswordEncoder()
    }
    
    /**
     * API 요청을 위한 보안 설정
     * API 요청에 대해서는 세션을 생성하지 않고 JWT 기반 인증 사용
     */
    @Bean
    @Order(1)
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("API 보안 필터 체인 설정")
        
        http
            .securityMatcher("/api/**")
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    // 오픈뱅킹 API 인증 및 콜백 관련 경로
                    .requestMatchers("/api/openbanking/auth").permitAll()
                    .requestMatchers("/api/openbanking/callback").permitAll()
                    // 기타 API 경로는 인증 필요
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        return http.build()
    }

    /**
     * OAuth2 및 일반 웹 요청을 위한 보안 설정
     */
    @Bean
    @Order(2)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("웹 보안 필터 체인 설정")
        
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .invalidSessionUrl("/auth/token-display?error=invalid_session")
                    .maximumSessions(1)
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    // 회원가입, 로그인 및 OAuth2 콜백 관련 경로
                    .requestMatchers("/auth/**", "/oauth2/**").permitAll()
                    // Swagger 관련 경로
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/api-docs/swagger-config").permitAll()
                    .requestMatchers("/v3/api-docs/swagger-config").permitAll()
                    // 그 외 모든 요청은 허용
                    .anyRequest().permitAll()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint {
                        it.baseUri("/oauth2/authorize")
                        it.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                    }
                    .redirectionEndpoint {
                        it.baseUri("/login/oauth2/code/*")
                    }
                    .userInfoEndpoint {
                        it.userService(oAuth2UserService)
                    }
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
            .headers { headers ->
                headers.frameOptions { it.disable() } // H2 콘솔 접근을 위해 필요
            }
        
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("authorization", "content-type", "x-auth-token")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
} 