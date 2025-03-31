package com.pleasybank.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {
    
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
        // 실제 애플리케이션에서는 userId로 사용자 정보를 조회하여 UserDetails 객체를 생성해야 합니다.
        // 여기서는 간단한 예시로 userId만 담은 UserDetails 객체를 생성합니다.
        return object : UserDetails {
            override fun getAuthorities() = listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            override fun getPassword() = null
            override fun getUsername() = userId
            override fun isAccountNonExpired() = true
            override fun isAccountNonLocked() = true
            override fun isCredentialsNonExpired() = true
            override fun isEnabled() = true
        }
    }
} 