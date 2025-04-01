package com.pleasybank.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                val userDetails = createUserDetails(userId)
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: Exception) {
            logger.error("JWT 인증에 실패했습니다: ${ex.message}")
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
    
    private fun createUserDetails(userId: String): UserDetails {
        // userId는 토큰에서 추출한 사용자 ID입니다.
        // AccountController에서는 username을 userId로 변환하여 사용하고 있으므로
        // 일관성을 유지하기 위해 username에 userId를 설정합니다.
        return object : UserDetails {
            override fun getAuthorities() = 
                listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            override fun getPassword() = null
            override fun getUsername() = userId
            override fun isAccountNonExpired() = true
            override fun isAccountNonLocked() = true
            override fun isCredentialsNonExpired() = true
            override fun isEnabled() = true
        }
    }
} 