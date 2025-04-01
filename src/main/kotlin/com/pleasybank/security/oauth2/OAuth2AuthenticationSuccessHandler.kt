package com.pleasybank.security.oauth2

import com.pleasybank.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    // 하드코딩된 리다이렉트 URI
    private val authorizedRedirectUris = listOf("http://localhost:3000/oauth2/redirect")

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val redirectUri = getRedirectUri(request)
        if (!isAuthorizedRedirectUri(redirectUri)) {
            throw IllegalArgumentException("인증 리다이렉트 URI가 승인되지 않았습니다.")
        }

        val oAuth2User = authentication.principal as DefaultOAuth2User
        val userId = oAuth2User.attributes["id"].toString()
        val accessToken = jwtTokenProvider.createToken(userId, "ROLE_USER")
        val refreshToken = jwtTokenProvider.createRefreshToken(userId)
        
        val targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString()

        clearAuthenticationAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun getRedirectUri(request: HttpServletRequest): String {
        val redirectUri = request.getParameter("redirect_uri")
        return redirectUri ?: authorizedRedirectUris[0]
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