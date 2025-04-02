package com.pleasybank.security.oauth2

import com.pleasybank.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository::class.java)

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
        const val OAUTH2_PROVIDER_TYPE_COOKIE_NAME = "oauth2_provider"
        private const val cookieExpireSeconds = 1800
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        logger.info("인증 요청 로드 시도 (세션 ID: ${request.session.id})")
        
        try {
            // 쿠키에서 인증 요청 불러오기
            val cookie = CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            if (cookie != null) {
                val value = cookie.value
                logger.info("쿠키에서 인증 요청을 찾았습니다 (길이: ${value.length})")
                
                // 세션에 저장된 요청이 있으면 반환
                val sessionAuthRequest = request.getSession(false)?.getAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME) as? OAuth2AuthorizationRequest
                if (sessionAuthRequest != null) {
                    logger.info("세션에서 인증 요청을 찾았습니다")
                    return sessionAuthRequest
                }
                
                // 세션에 없으면 쿠키에서 디코딩하여 반환
                return try {
                    val decoded = Base64.getUrlDecoder().decode(value)
                    val authRequest = deserialize(decoded)
                    
                    // 세션에도 저장해둠
                    request.session.setAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authRequest)
                    
                    authRequest
                } catch (e: Exception) {
                    logger.error("쿠키 역직렬화 실패: ${e.message}", e)
                    null
                }
            } else {
                logger.warn("쿠키에서 인증 요청을 찾을 수 없음")
                return null
            }
        } catch (e: Exception) {
            logger.error("인증 요청 로드 중 예외 발생: ${e.message}", e)
            return null
        }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            logger.info("인증 요청이 null이므로 쿠키 삭제")
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            CookieUtils.deleteCookie(request, response, OAUTH2_PROVIDER_TYPE_COOKIE_NAME)
            
            // 세션에서도 삭제
            request.getSession(false)?.removeAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            return
        }

        try {
            // 세션에 저장
            request.session.setAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authorizationRequest)
            logger.info("세션에 인증 요청 저장 완료 (세션ID: ${request.session.id})")
            
            // 쿠키에 저장 (Base64로 직렬화)
            val serializedAuth = serialize(authorizationRequest)
            logger.info("인증 요청을 쿠키에 저장 (길이: ${serializedAuth.length})")
            
            // 쿠키 추가
            CookieUtils.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serializedAuth,
                cookieExpireSeconds,
                secure = false,  // HTTP 테스트 환경에서는 false
                httpOnly = true,
                sameSite = "Lax"  // SameSite 설정
            )

            // 리다이렉트 URI가 있으면 저장
            val redirectUriAfterLogin = request.getParameter("redirect_uri")
            if (!redirectUriAfterLogin.isNullOrBlank()) {
                logger.info("리다이렉트 URI 쿠키에 저장: $redirectUriAfterLogin")
                
                CookieUtils.addCookie(
                    response,
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin,
                    cookieExpireSeconds,
                    secure = false,
                    httpOnly = true,
                    sameSite = "Lax"
                )
            }
        } catch (e: Exception) {
            logger.error("인증 요청 저장 중 예외 발생: ${e.message}", e)
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        logger.info("인증 요청 제거 시도 (세션 ID: ${request.session.id})")
        return this.loadAuthorizationRequest(request).also { 
            removeAuthorizationRequestCookies(request, response)
        }
    }
    
    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        logger.info("인증 요청 쿠키 제거")
        
        // 세션에서 제거
        request.getSession(false)?.removeAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        
        // 쿠키에서 제거
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response, OAUTH2_PROVIDER_TYPE_COOKIE_NAME)
    }

    private fun serialize(obj: OAuth2AuthorizationRequest): String {
        return Base64.getUrlEncoder().encodeToString(org.springframework.util.SerializationUtils.serialize(obj))
    }

    private fun deserialize(bytes: ByteArray): OAuth2AuthorizationRequest? {
        return org.springframework.util.SerializationUtils.deserialize(bytes) as? OAuth2AuthorizationRequest
    }
} 