package com.pleasybank.core.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 토큰 기반 인증을 처리하는 필터
 * 요청 헤더의 JWT 토큰을 확인하고 유효한 경우 Spring Security Context에 인증 정보를 설정합니다.
 */
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = getJwtFromRequest(request)
            if (token != null && jwtTokenProvider.validateToken(token)) {
                val authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.error("인증 처리 중 오류 발생: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
} 