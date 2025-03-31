package com.pleasybank.security.oauth2

import com.pleasybank.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${app.oauth2.authorized-redirect-uris}") private val authorizedRedirectUris: List<String>
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val redirectUri = getRedirectUri(request)
        if (!isAuthorizedRedirectUri(redirectUri)) {
            throw IllegalArgumentException("인증 리다이렉트 URI가 승인되지 않았습니다.")
        }

        val oAuth2User = authentication.principal as DefaultOAuth2UserImpl
        val userId = oAuth2User.getId()
        val accessToken = jwtTokenProvider.createToken(userId.toString(), "ROLE_USER")
        val refreshToken = jwtTokenProvider.createRefreshToken(userId.toString())
        
        val targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString()

        clearAuthenticationAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun getRedirectUri(request: HttpServletRequest): String {
        val redirectUri = request.getParameter("redirect_uri")
        return redirectUri ?: defaultTargetUrl
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)
        return authorizedRedirectUris
            .map { URI.create(it) }
            .any { authorizedUri ->
                authorizedUri.host.equals(clientRedirectUri.host, ignoreCase = true) &&
                authorizedUri.port == clientRedirectUri.port
            }
    }

    private fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
    }
}

// OAuth2User 구현체 클래스 (토큰 생성을 위한 ID 추출 용도)
class DefaultOAuth2UserImpl(private val attributes: Map<String, Any>) {
    fun getId(): Long {
        return attributes["id"] as Long
    }
} 