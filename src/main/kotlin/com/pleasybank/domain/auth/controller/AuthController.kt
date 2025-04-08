package com.pleasybank.domain.auth.controller

import com.pleasybank.domain.auth.dto.LoginRequest
import com.pleasybank.domain.auth.dto.SignupRequest
import com.pleasybank.domain.auth.dto.TokenResponse
import com.pleasybank.domain.auth.service.AuthService
import com.pleasybank.integration.kakao.KakaoApiClient
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val kakaoApiClient: KakaoApiClient,
    
    @Value("\${app.oauth2.redirect-uri}")
    private val frontendRedirectUri: String,
    
    @Value("\${app.oauth2.kakao.client-id}")
    private val kakaoClientId: String,
    
    @Value("\${app.oauth2.kakao.redirect-uri}")
    private val kakaoRedirectUri: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val restTemplate = RestTemplate()

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") refreshToken: String): ResponseEntity<TokenResponse> {
        val token = refreshToken.substring(7) // "Bearer " 제거
        val tokenResponse = authService.refreshToken(token)
        return ResponseEntity.ok(tokenResponse)
    }
    
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        val actualToken = token.substring(7) // "Bearer " 제거
        authService.logout(actualToken)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/kakao/login")
    fun kakaoLogin(): ResponseEntity<Void> {
        logger.debug("카카오 로그인 요청 - clientId: $kakaoClientId, redirectUri: $kakaoRedirectUri")
        
        val encodedRedirectUri = URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8)
        val kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize" +
            "?client_id=$kakaoClientId" +
            "&redirect_uri=$encodedRedirectUri" +
            "&response_type=code"
        
        logger.debug("카카오 인증 URL: $kakaoAuthUrl")
        
        val headers = HttpHeaders()
        headers.add(HttpHeaders.LOCATION, kakaoAuthUrl)
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build()
    }
    
    @GetMapping("/kakao/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<*> {
        logger.debug("카카오 콜백 수신 - 인증 코드: $code")
        try {
            // KakaoApiClient를 사용하여 카카오 토큰 요청
            val tokenResponse = kakaoApiClient.getAccessToken(code, kakaoRedirectUri)
            logger.debug("카카오 토큰 응답 성공: accessToken=${tokenResponse.accessToken.take(10)}...")
            
            // 카카오 사용자 정보 요청
            val userInfoResponse = kakaoApiClient.getUserInfo(tokenResponse.accessToken)
            logger.debug("카카오 사용자 정보: id=${userInfoResponse.id}, nickname=${userInfoResponse.kakaoAccount.profile.nickname}")
            
            // 카카오 ID와 닉네임으로 JWT 토큰 생성
            val kakaoId = userInfoResponse.id.toString()
            val nickname = userInfoResponse.kakaoAccount.profile.nickname ?: "사용자"
            
            // 직접 JWT 토큰 생성
            val jwtResponse = authService.processExternalLogin("KAKAO", kakaoId, nickname)
            
            // 프론트엔드로 리다이렉트 (토큰을 포함하여)
            val redirectUrl = UriComponentsBuilder
                .fromUriString(frontendRedirectUri)
                .queryParam("token", jwtResponse.accessToken)
                .queryParam("refreshToken", jwtResponse.refreshToken)
                .build()
                .toUriString()
            
            logger.debug("프론트엔드 리다이렉트 URL: $redirectUrl")
            
            val headers = HttpHeaders()
            headers.location = URI(redirectUrl)
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build<Void>()
            
        } catch (e: Exception) {
            logger.error("카카오 로그인 처리 중 오류 발생", e)
            throw RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
} 