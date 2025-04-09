package com.pleasybank.core.config

import com.pleasybank.core.security.JwtAuthenticationFilter
import com.pleasybank.core.security.JwtTokenProvider
import com.pleasybank.core.security.oauth2.CustomOAuth2UserService
import com.pleasybank.core.security.oauth2.OAuth2AuthenticationSuccessHandler
import com.pleasybank.domain.auth.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
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
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val userDetailsService: CustomUserDetailsService
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/products/**",
                        "/login/**",  // 로그인 관련 모든 URL 허용
                        "/oauth2/**", // OAuth2 관련 모든 URL 허용
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/actuator/**",
                        "/error",
                        "/favicon.ico"
                    ).permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { it.userService(oAuth2UserService) }
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureUrl("/api/auth/login?error=oauth_failure")
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { request, response, exception ->
                    // 인증 실패 시 로그 추가
                    println("인증 실패: ${exception.message}, URI: ${request.requestURI}")
                    response.sendError(401, "인증이 필요합니다: ${exception.message}")
                }
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "http://localhost:3001", "http://localhost:8080")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin")
            exposedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
            maxAge = 3600L
        }
        
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        return userDetailsService
    }
} 