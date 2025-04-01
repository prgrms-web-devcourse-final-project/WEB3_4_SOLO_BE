package com.pleasybank.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.io.IOException

// CorsWebFilter 비활성화 - Spring Security의 CORS 설정을 사용
// @Component
// @Order(Ordered.HIGHEST_PRECEDENCE)
class CorsWebFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val response = res as HttpServletResponse
        val request = req as HttpServletRequest
        
        // Origin 헤더 가져오기
        val origin = request.getHeader("Origin")
        
        // Origin 헤더가 있으면 해당 값을 CORS 헤더에 설정
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin)
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*")
        }
        
        response.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS, PATCH")
        response.setHeader("Access-Control-Allow-Headers", "*")
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Disposition")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        
        if ("OPTIONS".equals(request.method, ignoreCase = true)) {
            response.status = HttpServletResponse.SC_OK
            return
        }
        
        chain.doFilter(req, res)
    }

    override fun init(filterConfig: FilterConfig?) {}

    override fun destroy() {}
} 