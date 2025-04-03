package com.pleasybank.core.security.oauth2

import com.pleasybank.core.security.jwt.JwtTokenProvider
import com.pleasybank.core.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    private val logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)
    
    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider
    
    @Autowired
    private lateinit var authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository
    
    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)
        
        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to {}")
            return
        }
        
        clearAuthenticationAttributes(request, response)
        redirectWithToken(request, response, targetUrl)
    }
    
    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        val token = tokenProvider.generateToken(authentication)
        logger.debug("Generated JWT token: {}")
        
        // 기본 리디렉션 URL을 설정합니다.
        val defaultRedirectUri = "http://localhost:3000/auth/token-display"
        
        // 토큰을 쿼리 파라미터로 추가합니다.
        return UriComponentsBuilder.fromUriString(defaultRedirectUri)
            .queryParam("token", token)
            .build()
            .toUriString()
    }
    
    private fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
    }
    
    private fun redirectWithToken(request: HttpServletRequest, response: HttpServletResponse, targetUrl: String) {
        logger.debug("Redirecting to: {}")
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
    
    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)
        
        // 프론트엔드 클라이언트의 호스트와 포트를 확인합니다.
        val allowedRedirectUris = listOf(
            "http://localhost:3000",
            "http://localhost:8080"
        )
        
        return allowedRedirectUris.any { allowedUri ->
            val authorizedURI = URI.create(allowedUri)
            (authorizedURI.host.equals(clientRedirectUri.host, ignoreCase = true)
                    && (authorizedURI.port == clientRedirectUri.port))
        }
    }
} 