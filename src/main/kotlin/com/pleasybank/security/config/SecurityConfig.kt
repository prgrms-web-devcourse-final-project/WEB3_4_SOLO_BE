package com.pleasybank.security.config

import com.pleasybank.security.jwt.JwtAuthenticationFilter
import com.pleasybank.security.jwt.JwtTokenProvider
import com.pleasybank.security.oauth2.CustomOAuth2UserService
import com.pleasybank.security.oauth2.OAuth2AuthenticationSuccessHandler
import com.pleasybank.security.oauth2.OAuth2AuthenticationFailureHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler
) {
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth -> 
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/auth/oauth2/**").permitAll()
                    .requestMatchers("/api/auth/reset-password-request", "/api/auth/reset-password").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/system/health", "/api/system/version").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { endpoint ->
                        endpoint.baseUri("/api/auth/oauth2/authorize")
                    }
                    .redirectionEndpoint { endpoint ->
                        endpoint.baseUri("/api/auth/oauth2/callback/*")
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
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000") // 프론트엔드 서버 주소
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
} 