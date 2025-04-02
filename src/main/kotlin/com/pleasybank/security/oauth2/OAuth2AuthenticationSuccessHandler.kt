package com.pleasybank.security.oauth2

import com.pleasybank.security.jwt.JwtTokenProvider
import com.pleasybank.util.CookieUtils
import com.pleasybank.authentication.repository.OAuthProviderRepository
import com.pleasybank.authentication.repository.UserOAuthRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    private val oAuthProviderRepository: OAuthProviderRepository,
    private val userOAuthRepository: UserOAuthRepository,
    @Value("\${app.oauth2.authorizedRedirectUris[0]}") private val defaultRedirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {
    
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 허용된 리다이렉트 URI 목록
    private val authorizedRedirectUris = listOf(
        "http://localhost:8080/auth/token-display",
        "http://localhost:8080/token-display",
        "http://localhost:3000/auth/token-display"
    )

    init {
        // 기본 성공 URL을 설정
        setDefaultTargetUrl("/auth/token-display")
    }

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            logger.info("OAuth2 인증 성공: ${authentication.name}")
            
            // 인증된 사용자 정보 추출
            val oAuth2User = authentication.principal as DefaultOAuth2User
            val userId = oAuth2User.attributes["id"]?.toString() ?: 
                throw IllegalArgumentException("사용자 ID를 찾을 수 없습니다")
                
            logger.info("카카오 사용자 ID: $userId")
            
            // JWT 토큰 생성
            val accessToken = jwtTokenProvider.createToken(userId, "ROLE_USER")
            val refreshToken = jwtTokenProvider.createRefreshToken(userId)
            
            // 토큰 정보 및 오픈뱅킹 연동 상태를 쿼리 파라미터로 추가
            val targetUrl = determineTargetUrl(request, response, authentication)
            var redirectUrlBuilder = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                
            // 카카오 계정과 연결된 사용자 정보 조회
            val kakaoProvider = oAuthProviderRepository.findByProviderName("KAKAO")
                .orElseThrow { IllegalStateException("카카오 제공자를 찾을 수 없습니다") }
                
            val userOAuth = userOAuthRepository.findByProviderIdAndOauthUserId(kakaoProvider.id!!, userId)
            
            // 오픈뱅킹 연동 상태 확인 및 URL 구성
            if (userOAuth.isPresent) {
                val oauthInfo = userOAuth.get()
                val hasOpenBanking = oauthInfo.isOpenBankingLinked
                
                // 오픈뱅킹 연동 상태 파라미터 추가
                redirectUrlBuilder = redirectUrlBuilder.queryParam("openBankingLinked", hasOpenBanking)
                
                // 연동되지 않은 경우 오픈뱅킹 연동 필요 플래그 추가
                if (!hasOpenBanking) {
                    redirectUrlBuilder = redirectUrlBuilder.queryParam("requireOpenBankingAuth", true)
                }
            } else {
                // 사용자 정보가 없는 경우 (이런 경우는 없어야 함)
                logger.error("카카오 인증 성공했으나 사용자 정보를 찾을 수 없음: $userId")
                redirectUrlBuilder = redirectUrlBuilder.queryParam("error", "사용자 정보를 찾을 수 없습니다")
            }
            
            val redirectUrl = redirectUrlBuilder.build().toUriString()
            logger.info("최종 리다이렉트 URL: $redirectUrl")
            
            // 인증 요청 정보 삭제
            httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
            
            // 기존 인증 요청 정보 제거하기
            super.clearAuthenticationAttributes(request)
            
            redirectStrategy.sendRedirect(request, response, redirectUrl)
        } catch (ex: Exception) {
            logger.error("OAuth2 인증 성공 처리 중 오류 발생", ex)
            // 에러가 발생한 경우 에러 파라미터와 함께 토큰 표시 페이지로 리다이렉트
            val errorRedirectUrl = UriComponentsBuilder.fromUriString("/auth/token-display")
                .queryParam("error", "인증 처리 중 오류가 발생했습니다: ${ex.message}")
                .build().toUriString()
            redirectStrategy.sendRedirect(request, response, errorRedirectUrl)
        }
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        // 쿠키에서 리다이렉트 URI 확인
        val cookieRedirectUri = CookieUtils.getCookie(
            request,
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME
        )?.value
        
        logger.info("리다이렉트 URI (쿠키): $cookieRedirectUri")
        
        if (cookieRedirectUri != null && isAuthorizedRedirectUri(cookieRedirectUri)) {
            logger.info("유효한 리다이렉트 URI 사용: $cookieRedirectUri")
            return cookieRedirectUri
        }
        
        logger.info("기본 리다이렉트 URI 사용: $defaultRedirectUri")
        return defaultRedirectUri
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        try {
            logger.debug("리다이렉트 URI 확인: $uri")
            val clientRedirectUri = URI.create(uri)
            val result = authorizedRedirectUris
                .map { URI.create(it) }
                .any { authorizedUri ->
                    authorizedUri.host.equals(clientRedirectUri.host, ignoreCase = true) &&
                    authorizedUri.port == clientRedirectUri.port
                }
            logger.debug("리다이렉트 URI 유효성: $result")
            return result
        } catch (ex: Exception) {
            logger.error("URI 검증 중 오류 발생: $uri", ex)
            return false
        }
    }
} 