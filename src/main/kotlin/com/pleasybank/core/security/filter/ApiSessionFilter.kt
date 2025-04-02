package com.pleasybank.core.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

/**
 * API 요청에 대해 세션 관리 필터를 우회하는 필터
 * /api/ 경로로 시작하는 모든 요청에 대해 세션 검증을 건너뛰게 함
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiSessionFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(ApiSessionFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI

        // API 요청인 경우 세션 관련 정보를 삭제하여 세션 검증을 건너뛰게 함
        if (requestURI.startsWith("/api/")) {
            logger.info("API 요청 감지, 세션 검증 스킵: $requestURI")
            
            // 세션 ID를 포함한 쿠키가 있는 경우, 해당 쿠키를 무효화
            val cookies = request.cookies
            if (cookies != null) {
                for (cookie in cookies) {
                    if (cookie.name == "JSESSIONID") {
                        // 세션 관련 쿠키를 제거하도록 응답에 설정
                        val clearCookie = jakarta.servlet.http.Cookie(cookie.name, "")
                        clearCookie.maxAge = 0
                        clearCookie.path = "/"
                        response.addCookie(clearCookie)
                        break
                    }
                }
            }
            
            // 요청 래퍼를 사용하여 세션 ID를 숨김
            val wrappedRequest = ApiRequestWrapper(request)
            filterChain.doFilter(wrappedRequest, response)
        } else {
            // API 요청이 아닌 경우 정상적으로 처리
            filterChain.doFilter(request, response)
        }
    }

    /**
     * API 요청을 위한 HttpServletRequest 래퍼
     * getRequestedSessionId()를 오버라이드하여 null을 반환함으로써
     * 세션 관리 필터가 invalid session으로 판단하지 않도록 함
     */
    private class ApiRequestWrapper(request: HttpServletRequest) : 
        jakarta.servlet.http.HttpServletRequestWrapper(request) {
        
        // 세션 ID 요청 메서드를 오버라이드하여 null을 반환
        override fun getRequestedSessionId(): String? {
            return null
        }
        
        // 세션 관련 메서드도 오버라이드
        override fun isRequestedSessionIdValid(): Boolean {
            return false
        }
        
        override fun getSession(create: Boolean): jakarta.servlet.http.HttpSession? {
            return if (create) super.getSession(true) else null
        }
        
        override fun getSession(): jakarta.servlet.http.HttpSession? {
            return null
        }
    }
} 