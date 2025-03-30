package com.pleasybank.security.oauth2

import com.pleasybank.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2SuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${oauth2.redirect-uri}") private val redirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {
    
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val attributes = oAuth2User.attributes
        
        val email = attributes["email"] as String
        
        // JWT 토큰 생성
        val accessToken = jwtTokenProvider.createToken(email, "ROLE_USER")
        val refreshToken = jwtTokenProvider.createRefreshToken(email)
        
        // 리다이렉트 URI 생성
        val redirectUrl = buildRedirectUrl(accessToken, refreshToken)
        
        // 리다이렉트
        response.sendRedirect(redirectUrl)
    }
    
    private fun buildRedirectUrl(accessToken: String, refreshToken: String): String {
        val encodedAccessToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
        val encodedRefreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
        
        return "$redirectUri?accessToken=$encodedAccessToken&refreshToken=$encodedRefreshToken"
    }
} 