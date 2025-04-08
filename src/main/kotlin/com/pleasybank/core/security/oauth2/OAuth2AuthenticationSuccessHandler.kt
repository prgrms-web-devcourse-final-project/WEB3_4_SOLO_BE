package com.pleasybank.core.security.oauth2

import com.pleasybank.core.security.JwtTokenProvider
import com.pleasybank.domain.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

/**
 * OAuth2 인증 성공 핸들러
 * 인증 성공 후 JWT 토큰을 발급하고 프론트엔드 리다이렉트 URL로 토큰을 전달
 */
@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    
    @Value("\${app.oauth2.redirect-uri:http://localhost:3000/auth/oauth-callback}")
    private val redirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val targetUrl = determineTargetUrl(request, response, authentication)
            if (response.isCommitted) {
                logger.debug("응답이 이미 커밋되었습니다. $targetUrl 로 리다이렉트할 수 없습니다")
                return
            }
            
            clearAuthenticationAttributes(request)
            redirectStrategy.sendRedirect(request, response, targetUrl)
        } catch (ex: Exception) {
            logger.error("OAuth2 인증 성공 처리 중 오류 발생", ex)
            response.sendRedirect("$redirectUri?error=login_failed&message=${ex.message}")
        }
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        try {
            val oAuth2User = authentication.principal as OAuth2User
            
            // 로그에 디버그 정보 추가
            logger.debug("OAuth2 인증 성공: ${oAuth2User.attributes}")
            logger.debug("요청 URI: ${request.requestURI}")
            
            // provider 이름 추출
            val provider = when {
                request.requestURI.contains("/kakao") -> "kakao"
                request.requestURI.contains("/google") -> "google"
                else -> {
                    logger.warn("알 수 없는 provider URI: ${request.requestURI}, 기본값 'unknown' 사용")
                    "unknown"
                }
            }
            
            logger.debug("Provider 추출: $provider, URI: ${request.requestURI}")
            
            // 카카오의 경우 id는 숫자 타입이므로 다르게 처리
            val providerId = when (provider) {
                "kakao" -> {
                    val id = oAuth2User.attributes["id"]
                    logger.debug("카카오 ID 추출: $id, 타입: ${id?.javaClass?.name}")
                    id?.toString()
                }
                else -> oAuth2User.getAttribute<String>("sub")
            }
            
            if (providerId == null) {
                logger.error("providerId를 찾을 수 없습니다: ${oAuth2User.attributes}")
                throw IllegalArgumentException("사용자 ID를 찾을 수 없습니다")
            }
            
            logger.debug("Provider ID: $providerId")
            
            // 사용자 조회 또는 생성
            val user = processUser(provider, providerId, oAuth2User)
            val userId = user.id ?: throw IllegalArgumentException("사용자 ID가 null입니다")
            
            // JWT 토큰 생성
            val token = jwtTokenProvider.createToken(userId, listOf("ROLE_USER"))
            logger.debug("JWT 토큰 생성 완료: ${token.take(10)}...")
            
            // 리다이렉트 URL 생성
            logger.debug("리다이렉트 URI: $redirectUri")
            return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString()
        } catch (ex: Exception) {
            logger.error("determineTargetUrl 처리 중 오류 발생", ex)
            return "$redirectUri?error=login_failed&message=${ex.message}"
        }
    }
    
    private fun processUser(provider: String, providerId: String, oAuth2User: OAuth2User): com.pleasybank.domain.user.entity.User {
        // providerId로 사용자 조회 시도
        return userRepository.findByProviderAndProviderId(provider, providerId)
            ?: createNewUser(provider, providerId, oAuth2User)
    }
    
    private fun createNewUser(provider: String, providerId: String, oAuth2User: OAuth2User): com.pleasybank.domain.user.entity.User {
        // 사용자가 없으면 새로 생성
        logger.debug("기존 사용자를 찾을 수 없어 새로 생성합니다. provider: $provider, providerId: $providerId")
        
        val tempEmail = "${provider}_${providerId}@pleasybank.com"
        val name = when (provider) {
            "kakao" -> {
                try {
                    val properties = oAuth2User.attributes["properties"] as? Map<*, *>
                    properties?.get("nickname")?.toString() ?: "카카오사용자"
                } catch (e: Exception) {
                    logger.warn("카카오 닉네임 추출 실패", e)
                    "카카오사용자"
                }
            }
            else -> oAuth2User.getAttribute<String>("name") ?: "사용자"
        }
        
        val newUser = com.pleasybank.domain.user.entity.User(
            email = tempEmail,
            name = name,
            password = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(java.util.UUID.randomUUID().toString()),
            provider = provider,
            providerId = providerId,
            status = "ACTIVE",
            lastLoginAt = java.time.LocalDateTime.now(),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
        
        return userRepository.save(newUser)
    }
} 