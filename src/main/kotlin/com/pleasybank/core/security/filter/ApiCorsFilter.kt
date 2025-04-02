package com.pleasybank.core.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * API 요청에 대한 CORS 헤더를 추가하는 필터
 * 모든 요청에 CORS 헤더를 추가합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiCorsFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(ApiCorsFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")
        logger.info("CORS 필터 실행 - 경로: ${request.requestURI}, 메서드: ${request.method}, 오리진: $origin")
        
        // 요청의 오리진을 그대로 사용 (와일드카드 * 대신)
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin)
            logger.info("오리진 허용: $origin")
        } else {
            // 기본 로컬 개발 오리진 설정
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000")
            logger.info("기본 오리진 설정: http://localhost:3000")
        }
        
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, X-Requested-With")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Max-Age", "3600")
        
        // OPTIONS 요청 처리 (프리플라이트 요청)
        if (request.method.equals("OPTIONS", ignoreCase = true)) {
            logger.info("OPTIONS 요청 처리: ${request.requestURI}")
            response.status = HttpServletResponse.SC_OK
            return
        }
        
        // 필터 체인 계속 진행
        filterChain.doFilter(request, response)
    }
} 