package com.pleasybank.error.controller

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class CustomErrorController : ErrorController {
    
    private val logger = LoggerFactory.getLogger(CustomErrorController::class.java)
    
    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        val statusCode = request.getAttribute("jakarta.servlet.error.status_code") as? Int ?: 500
        val message = request.getAttribute("jakarta.servlet.error.message") as? String ?: "서버 오류가 발생했습니다"
        val exception = request.getAttribute("jakarta.servlet.error.exception") as? Throwable
        val uri = request.getAttribute("jakarta.servlet.error.request_uri") as? String ?: request.requestURI
        
        // 오류 정보 로깅
        logger.error("에러 발생: 상태 코드=$statusCode, URI=$uri, 메시지=$message", exception)
        
        // 세션 정보 확인
        val sessionId = request.session?.id ?: "세션 없음"
        logger.info("에러 처리 세션 ID: $sessionId")
        
        // OAuth2 관련 에러 확인
        val errorParam = request.getParameter("error")
        if (errorParam != null && errorParam.contains("authorization_request_not_found")) {
            // 에러 메시지 설정
            model.addAttribute("errorMessage", "인증 요청 정보를 찾을 수 없습니다 (세션 만료)")
            return "auth/token-error"
        }
        
        // 모델에 오류 정보 추가
        model.addAttribute("statusCode", statusCode)
        model.addAttribute("errorMessage", message)
        model.addAttribute("path", uri)
        
        return when (statusCode) {
            404 -> "error/404"
            401, 403 -> "error/403"
            400 -> {
                if (uri.contains("token-display")) {
                    model.addAttribute("errorMessage", "인증 요청 정보가 세션에서 유실되었습니다")
                    "auth/token-error"
                } else {
                    "error/400"
                }
            }
            500 -> "auth/token-error" // 500 오류는 토큰 에러 페이지로 리다이렉트
            else -> "auth/token-error"
        }
    }
} 