package com.pleasybank.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.PrintWriter
import java.io.StringWriter

@Component
class CustomOAuth2AuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository
) : OAuth2AuthorizationRequestResolver {

    private val logger = LoggerFactory.getLogger(CustomOAuth2AuthorizationRequestResolver::class.java)
    
    private val defaultResolver = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, "/oauth2/authorize"
    )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        logger.info("OAuth2 인증 요청 처리 시작: ${request.requestURI}")
        
        // 오픈뱅킹 API 관련 경로는 OAuth2 인증 처리에서 제외
        if (request.requestURI.startsWith("/api/openbanking/")) {
            logger.info("오픈뱅킹 API 경로 감지됨 - OAuth2 인증 요청 처리 건너뜀: ${request.requestURI}")
            return null
        }
        
        // 카카오 인증 코드 콜백 URL인 경우 처리 중단
        if (request.requestURI.startsWith("/login/oauth2/code/")) {
            logger.info("OAuth2 콜백 URL 감지됨 - 인증 요청 생성 건너뜀")
            return null
        }
        
        // 기본 리졸버 사용
        return defaultResolver.resolve(request)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        logger.info("OAuth2 인증 요청 처리 시작 (clientRegistrationId: $clientRegistrationId): ${request.requestURI}")
        
        // 오픈뱅킹 API 관련 경로는 OAuth2 인증 처리에서 제외
        if (request.requestURI.startsWith("/api/openbanking/")) {
            logger.info("오픈뱅킹 API 경로 감지됨 - OAuth2 인증 요청 처리 건너뜀: ${request.requestURI}")
            return null
        }
        
        // 카카오 인증 코드 콜백 URL인 경우 처리 중단
        if (request.requestURI.startsWith("/login/oauth2/code/")) {
            logger.info("OAuth2 콜백 URL 감지됨 - 인증 요청 생성 건너뜀")
            return null
        }
        
        // 기본 리졸버 사용
        return defaultResolver.resolve(request, clientRegistrationId)
    }
    
    // 현재 요청 컨텍스트에서 응답 객체를 얻는 함수
    private fun getCurrentResponse(): HttpServletResponse? {
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            return requestAttributes?.response
        } catch (e: Exception) {
            logger.error("현재 요청 컨텍스트에서 응답 객체를 가져오는 중 오류 발생", e)
            return null
        }
    }
} 