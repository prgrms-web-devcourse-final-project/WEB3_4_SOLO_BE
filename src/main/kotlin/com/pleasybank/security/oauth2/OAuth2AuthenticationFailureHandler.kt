package com.pleasybank.security.oauth2

import com.pleasybank.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler(
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository
) : SimpleUrlAuthenticationFailureHandler() {

    private val logger = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler::class.java)

    init {
        // 기본 실패 URL 설정
        setDefaultFailureUrl("/auth/token-display?error=login_failure")
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        try {
            logger.error("OAuth2 인증 실패: ${exception.message}", exception)
            
            // 에러 정보를 담아 토큰 표시 페이지로 리다이렉트
            val targetUrl = "/auth/token-display"
            val redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.message ?: "인증 과정에서 오류가 발생했습니다")
                .build().toUriString()
            
            logger.info("인증 실패 리다이렉트: $redirectUrl")
            
            redirectStrategy.sendRedirect(request, response, redirectUrl)
        } catch (e: Exception) {
            logger.error("인증 실패 처리 중 예외 발생", e)
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 실패 처리 중 오류: " + e.message)
        }
    }
} 