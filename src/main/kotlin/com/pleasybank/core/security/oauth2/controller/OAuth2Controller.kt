package com.pleasybank.core.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/oauth2")
class OAuth2Controller {
    private val logger = LoggerFactory.getLogger(OAuth2Controller::class.java)
    
    // 하드코딩된 값으로 대체
    private val kakaoClientId = "be56a79b5d2ef5456c6c2cf55d89dd38"
    private val kakaoAuthorizationUri = "https://kauth.kakao.com/oauth/authorize"
    private val kakaoRedirectUri = "http://localhost:8080/login/oauth2/code/kakao"
    private val kakaoScope = listOf("profile_nickname", "profile_image", "account_email")
    
    @GetMapping("/authorize/{provider}")
    fun authorizeProvider(@PathVariable provider: String, 
                         @RequestParam(required = false) redirectUri: String?,
                         request: HttpServletRequest): RedirectView {
        logger.info("OAuth2 인증 요청: provider={}, redirectUri={}", provider, redirectUri)
        
        when (provider.lowercase()) {
            "kakao" -> {
                val scope = kakaoScope.joinToString(",")
                val authUri = StringBuilder(kakaoAuthorizationUri)
                    .append("?response_type=code")
                    .append("&client_id=").append(kakaoClientId)
                    .append("&redirect_uri=").append(kakaoRedirectUri)
                    .append("&scope=").append(scope)
                
                logger.debug("카카오 인증 URI: {}", authUri)
                return RedirectView(authUri.toString())
            }
            else -> {
                logger.error("지원하지 않는 OAuth2 제공자: {}", provider)
                val errorView = RedirectView("/error")
                errorView.setStatusCode(HttpStatus.BAD_REQUEST)
                return errorView
            }
        }
    }
} 