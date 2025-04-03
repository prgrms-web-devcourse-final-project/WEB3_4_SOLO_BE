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
            
            try {
                // 요청 래퍼를 사용하여 세션 관련 동작을 제어
                val wrappedRequest = ApiRequestWrapper(request)
                filterChain.doFilter(wrappedRequest, response)
            } catch (e: Exception) {
                logger.error("API 요청 처리 중 오류 발생: ${e.message}", e)
                filterChain.doFilter(request, response)
            }
        } else {
            // API 요청이 아닌 경우 정상적으로 처리
            filterChain.doFilter(request, response)
        }
    }

    /**
     * API 요청을 위한 HttpServletRequest 래퍼
     * 세션 관련 메서드를 안전하게 오버라이드하여 
     * ForceEagerSessionCreationFilter와의 충돌을 방지
     */
    private class ApiRequestWrapper(request: HttpServletRequest) : 
        jakarta.servlet.http.HttpServletRequestWrapper(request) {
        
        private val logger = LoggerFactory.getLogger(ApiRequestWrapper::class.java)
        
        // 세션 ID 요청 메서드를 오버라이드하여 빈 문자열 반환 (null 아님)
        override fun getRequestedSessionId(): String {
            return ""
        }
        
        // 세션 관련 메서드도 오버라이드
        override fun isRequestedSessionIdValid(): Boolean {
            return true // 항상 유효하다고 응답
        }
        
        override fun getSession(create: Boolean): jakarta.servlet.http.HttpSession? {
            return try {
                // 필요한 경우에만 세션 생성
                if (create) {
                    logger.debug("API 요청에서 세션 생성 시도")
                    super.getSession(true)
                } else {
                    // 세션을 생성하지 않고 기존 세션이 있으면 반환
                    val existingSession = super.getSession(false)
                    logger.debug("API 요청에서 기존 세션 반환: ${existingSession != null}")
                    existingSession
                }
            } catch (e: Exception) {
                logger.warn("세션 접근 중 오류 발생: ${e.message}")
                null
            }
        }
        
        override fun getSession(): jakarta.servlet.http.HttpSession? {
            return getSession(false)
        }
        
        // 세션 쿠키 관련 메서드도 오버라이드
        override fun isRequestedSessionIdFromCookie(): Boolean {
            return false
        }
        
        override fun isRequestedSessionIdFromURL(): Boolean {
            return false
        }
        
        override fun changeSessionId(): String {
            // 실제로 세션 ID를 변경하지 않고 빈 문자열 반환
            return ""
        }
    }
} 