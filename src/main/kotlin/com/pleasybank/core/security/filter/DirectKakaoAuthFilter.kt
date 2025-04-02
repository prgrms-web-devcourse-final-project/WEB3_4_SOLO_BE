package com.pleasybank.core.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import org.springframework.util.StreamUtils
import java.nio.charset.Charset

/**
 * 카카오 인증 요청을 가로채서 카카오 로그인 페이지로 리다이렉션되지 않도록 하는 필터
 * 이 필터는 /api/auth/kakao/process와 같은 특정 경로에 대해 OAuth2 인증 필터를 우회하게 합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // ApiCorsFilter 다음에 실행되도록 순서 조정
class DirectKakaoAuthFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(DirectKakaoAuthFilter::class.java)
    
    // 이 필터가 적용될 경로 패턴
    private val kakaoProcessMatcher = AntPathRequestMatcher("/api/auth/kakao/process")
    
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val isKakaoProcessRequest = kakaoProcessMatcher.matches(request)
        val isOptionsRequest = request.method.equals("OPTIONS", ignoreCase = true)
        
        if (isKakaoProcessRequest) {
            logger.info("카카오 인증 처리 요청 감지: ${request.method} ${request.requestURI} (${request.remoteAddr})")
            
            // OPTIONS 요청은 이미 ApiCorsFilter에서 처리했으므로 이중 처리하지 않음
            if (!isOptionsRequest) {
                // OAuth2 리다이렉트 방지 플래그 설정
                request.setAttribute("skipOAuth2Redirect", true)
                request.setAttribute("directKakaoAuthProcessing", true)
                logger.info("카카오 인증 처리 요청 플래그 설정 완료")
            }
        }
        
        // 다음 필터 계속 진행
        filterChain.doFilter(request, response)
    }

    /**
     * CORS 헤더를 응답에 추가하는 헬퍼 메서드
     */
    private fun addCorsHeaders(response: HttpServletResponse, origin: String?) {
        // 와일드카드(*) 대신 실제 오리진을 설정
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin)
        } else {
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000")
        }
        
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept, X-Requested-With, X-Debug-Origin")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Max-Age", "3600")
    }
} 